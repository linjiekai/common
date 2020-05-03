package com.xfhl.common.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.xfhl.common.api.config.Platform;
import com.xfhl.common.api.config.StorageProperties;
import com.xfhl.common.api.dto.req.StorageDTO;
import com.xfhl.common.api.enums.OSSPathEnum;
import com.xfhl.common.api.enums.PlatformType;
import com.xfhl.common.api.exception.CommonException;
import com.xfhl.common.api.service.IStorageService;
import com.xfhl.common.api.utils.CharUtil;
import com.xfhl.common.api.utils.RedisUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.*;

/**
 * @author Yogeek
 * @date 2018/7/16 16:10
 * @decrpt 阿里云对象存储服务
 */
@Data
@Slf4j
@Service
public class AliyunIStorageServiceImpl implements IStorageService {


    @Autowired
    StorageProperties storageProperties;

    @Override
    public String getBaseUrl() {
        return storageProperties.getPlatforms().get(0).getUrl();
    }


    public Platform getAliyun(String mercId, String mercName) {
        List<Platform> platforms = storageProperties.getPlatforms();
        for (Platform item : platforms) {
            if (mercId.equalsIgnoreCase(item.getMercId()) && mercName.equalsIgnoreCase(item.getMercCode())) {
                return item;
            }

        }
        //默认取第一个吧
        return storageProperties.getPlatforms().get(0);
    }

    /**
     * 阿里云OSS对象存储简单上传实现
     */
    @Override
    public String store(MultipartFile file, StorageDTO dto) {
        int isPrivate = dto.getIsPrivate();
        String keyName = dto.getKeyName();
        Platform platform = getAliyun(dto.getMercId(), dto.getPlatform());
        long l = System.currentTimeMillis();
        log.info("开始上传文件,keyName：{},是否私有：{}", keyName, isPrivate);
        OSS oss = null;
        String urlstr = "";
        try {
            // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20M以下的文件使用该接口
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());
            long partSize = 1 * 1024 * 1024L;
            // 对象键（Key）是对象在存储桶中的唯一标识。
            PutObjectResult putObjectResult = null;
            if (isPrivate == 1) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(platform.getPrivateBucketName(), keyName, file.getInputStream(), objectMetadata);
                oss = new OSSClientBuilder().build(storageProperties.getPrivateEndpoint(), storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
                putObjectResult = oss.putObject(putObjectRequest);
                log.info("store:etag:{}, requestId:{}", putObjectResult.getETag(), putObjectResult.getRequestId());
            } else {
                if (file.getSize() > partSize) {
                    multipartUpload(file, keyName, objectMetadata, platform);
                } else {
                    commonUpload(file, keyName, objectMetadata, platform);
                }
            }
            urlstr = platform.getUrl() + keyName;
            log.info("上传文件结束,urlstr：{},耗时；{}", urlstr, System.currentTimeMillis() - l);
        } catch (Exception ex) {
            log.error("上传OSS失败:{}", ex);
            throw new CommonException("图片上传失败");
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
            return urlstr;
        }

    }


    public Resource loadAsResource(StorageDTO dto) {
        try {
            URL url = new URL(getBaseUrl() + dto.getKeyName());
            Resource resource = new UrlResource(url);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public void delete(StorageDTO dto) {
        OSS ossClient = null;
        Platform platform = getAliyun(dto.getMercId(), dto.getPlatform());
        try {
            ossClient = new OSSClientBuilder().build(storageProperties.getEndpoint(), storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
            ossClient.deleteObject(platform.getBucketName(), dto.getKeyName());
        } catch (Exception e) {
            log.error("删除 oss 资源失败，keyName：{}，bucketName：{}", dto.getKeyName(), platform.getBucketName());
        } finally {
            ossClient.shutdown();
        }

    }

    @Override
    public String generateUrl(StorageDTO dto) {
        return getBaseUrl() + dto.getKeyName();
    }

    @Override
    public String getStsToken(StorageDTO dto) throws Exception {
        try {
            Platform platform = getAliyun(dto.getMercId(), dto.getPlatform());
            DefaultProfile.addEndpoint("", "", "Sts", platform.getStsDomain());
            // 构造default profile（参数留空，无需添加region ID）
            IClientProfile profile = DefaultProfile.getProfile("", storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
            // 用profile构造client
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.GET);
            request.setRoleArn(platform.getRoleArn());
            request.setRoleSessionName(platform.getRoleSessionName());
            request.setDurationSeconds(platform.getExpires()); // 设置凭证有效时间
            final AssumeRoleResponse response = client.getAcsResponse(request);
            log.info("AssumeRoleResponse:{}", JSON.toJSONString(response));
            String securityToken = response.getCredentials().getSecurityToken();
            String accessKeyId1 = response.getCredentials().getAccessKeyId();
            String accessKeySecret1 = response.getCredentials().getAccessKeySecret();
            //存缓存
            Map<String, String> map = new HashMap<>();
            map.put("token", securityToken);
            map.put("url", dto.getOssPath());
            map.put("privateAccessKeyId", accessKeyId1);
            map.put("privateAccessKeySecret", accessKeySecret1);
            String token = CharUtil.getRandomString(32);
            RedisUtil.set(token, map, platform.getExpires());
            return token;
        } catch (ClientException e) {
            log.error("获取STS资源临时token失败：{}", e);
            throw new CommonException(e.getMessage());
        }
    }

    public Map<String, Object> getOssResouces(String securityToken, String url, boolean isPrivate) throws Exception {
        OSS oss = null;
        OSSObject object = null;
        try {
            Platform platform = getAliyun(PlatformType.XFYLMALL.getId(), PlatformType.XFYLMALL.getCode());
            if (isPrivate) {
                Map<String, String> map = (Map) RedisUtil.get(securityToken);
                String token2 = map.get("token");
                String url1 = map.get("url");
                String privateAccessKeyId = map.get("privateAccessKeyId");
                String privateAccessKeySecret = map.get("privateAccessKeySecret");
                oss = new OSSClientBuilder().build(storageProperties.getPrivateEndpoint(), privateAccessKeyId, privateAccessKeySecret, token2);
                object = oss.getObject(platform.getPrivateBucketName(), new URL(url1).getPath().replaceFirst("/", ""));
            } else {
                oss = new OSSClientBuilder().build(storageProperties.getEndpoint(), storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
                object = oss.getObject(platform.getBucketName(), url);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("OSSObject", object);
            result.put("oss", oss);
            return result;
        } catch (OSSException e) {
            log.error("获取oss图片失败 securityToken：{}，url：{}，error：{}", securityToken, url, e);
            throw new CommonException(e.getErrorMessage());
        } catch (Exception e) {
            log.error("获取oss图片失败 securityToken：{}，url：{}，error：{}", securityToken, url, e);
            throw e;
        }
    }

    //普通上传
    public void commonUpload(MultipartFile file, String keyName, ObjectMetadata objectMetadata, Platform platform) throws Exception {
        OSS ossClient = null;
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(platform.getBucketName(), keyName, file.getInputStream(), objectMetadata);
            ossClient = new OSSClientBuilder().build(storageProperties.getEndpoint(), storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            log.info("store:etag:{}, requestId:{}", putObjectResult.getETag(), putObjectResult.getRequestId());
        } catch (Exception e) {
            log.error("普通上传失败，keyName：{}，bucketName：{}", keyName, platform.getBucketName());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }

    //分片上传
    public void multipartUpload(MultipartFile file, String keyName, ObjectMetadata objectMetadata, Platform platform) throws Exception {
        OSS ossClient = null;
        String bucketName = platform.getBucketName();
        try {
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, keyName);
            request.setObjectMetadata(objectMetadata);
            ossClient = new OSSClientBuilder().build(storageProperties.getEndpoint(), storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
            String uploadId = upresult.getUploadId();

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags = new ArrayList<PartETag>();
            // 计算文件有多少个分片。
            final long partSize = 1 * 1024 * 1024L;   // 1MB
            //   final File sampleFile = new File("<localFile>");
            long fileLength = file.getSize();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;

                InputStream instream = file.getInputStream();
                // 跳过已经上传的分片。
                instream.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(keyName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }
            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, keyName, uploadId, partETags);

            // 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);

            // 完成上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            log.info("store:etag:{}, requestId:{}", completeMultipartUploadResult.getETag(), completeMultipartUploadResult.getRequestId());

        } catch (Exception e) {
            log.error("分片上传失败，keyName：{}，bucketName：{}", keyName, bucketName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * @param :[map]
     * @return :java.lang.Object
     * @Description(描述): 前端获取临时上传权限
     * @auther: Jack Lin
     * @date: 2019/11/22 16:17
     */
    @Override
    public Object stsPostPolicy(StorageDTO dto) throws Exception {
        String accessKeyId = storageProperties.getAccessKeyId();
        Platform platform = getAliyun(dto.getMercId(), dto.getPlatform());
        URL url = new URL(storageProperties.getEndpoint());
        String path = url.getAuthority();
        String dir = dto.getOssPath();

        String host = "http://" + platform.getBucketName() + "." + path; // host的格式为 bucketname.endpoint
        OSS client = new OSSClientBuilder().build(storageProperties.getEndpoint(), accessKeyId, storageProperties.getAccessKeySecret());
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            respMap.put("fileName", dto.getFileName());

            return respMap;
        } catch (Exception e) {
            log.error("获取获取sts上传文件临时权限失败：{}", e);
            return null;
        } finally {
            client.shutdown();
        }
    }

    /**
     * @param :[map]
     * @return :java.lang.Object
     * @Description(描述): app获取临时上传权限
     * @auther: Jack Lin
     * @date: 2019/11/22 16:17
     */
    @Override
    public Object stsUploadPolicyForApp(StorageDTO dto) throws Exception {
        String policy = "{\"Statement\":[{\"Action\":[\"oss:PutObject\",\"oss:ListParts\",\"oss:AbortMultipartUpload\"],\"Effect\":\"Allow\",\"Resource\":[\"acs:oss:*:*:static-xfyinli*\"]}],\"Version\":\"1\"}";
        try {

            OSSPathEnum one = OSSPathEnum.getOne(0, dto.getType(), dto.getIsPrivate() == 1 ? true : false);

            Platform platform = getAliyun(dto.getMercId(), dto.getPlatform());
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
            DefaultAcsClient client = new DefaultAcsClient(profile);
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion("2015-04-01");
            request.setMethod(MethodType.POST);
            request.setProtocol(ProtocolType.HTTPS);
            request.setRoleArn(platform.getRoleArn());
            request.setRoleSessionName(platform.getRoleSessionName());
            request.setPolicy(policy);
            request.setDurationSeconds(platform.getExpires());
            AssumeRoleResponse stsResponse = client.getAcsResponse(request);

            Map<String, String> respMap = new LinkedHashMap();
            respMap.put("AccessKeyId", stsResponse.getCredentials().getAccessKeyId());
            respMap.put("AccessKeySecret", stsResponse.getCredentials().getAccessKeySecret());
            respMap.put("SecurityToken", stsResponse.getCredentials().getSecurityToken());
            respMap.put("Expiration", stsResponse.getCredentials().getExpiration());
            respMap.put("path", platform.getMercCode().toLowerCase() + "/" + one.getPath());

            return respMap;
        } catch (Exception e) {
            log.error("app获取临时上传权限 失败：{}", e);
            return null;
        }
    }


    @Override
    public void ossResouces(StorageDTO dto, HttpServletResponse response) throws Exception {
        boolean b = false;
        OutputStream outputStream = null;
        InputStream objectContent = null;
        OSS oss = null;
        OSSObject object = null;
        try {
            if (StringUtils.isNotBlank(dto.getStsToken())) {
                b = true;
            }
            Platform platform = getAliyun(PlatformType.XFYLMALL.getId(), PlatformType.XFYLMALL.getCode());
            if (b) {
                Map<String, String> map = (Map) RedisUtil.get(dto.getStsToken());
                String token2 = map.get("token");
                String url1 = map.get("url");
                String privateAccessKeyId = map.get("privateAccessKeyId");
                String privateAccessKeySecret = map.get("privateAccessKeySecret");
                oss = new OSSClientBuilder().build(storageProperties.getPrivateEndpoint(), privateAccessKeyId, privateAccessKeySecret, token2);
                object = oss.getObject(platform.getPrivateBucketName(), new URL(url1).getPath().replaceFirst("/", ""));
            } else {
                oss = new OSSClientBuilder().build(storageProperties.getEndpoint(), storageProperties.getAccessKeyId(), storageProperties.getAccessKeySecret());
                object = oss.getObject(platform.getBucketName(), dto.getStsToken());
            }

            response.setContentType(object.getObjectMetadata().getContentType());
            outputStream = response.getOutputStream();
            objectContent = object.getObjectContent();

            byte[] bytes = new byte[1024 * 10];
            int len = 0;
            while ((len = objectContent.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            log.error("读取oss资源失败：{}", e);
            throw e;
        } finally {
            if (objectContent != null) {
                objectContent.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (oss != null) {
                oss.shutdown();
            }
            if (object != null) {
                object.close();
            }

        }
    }

    @Override
    public void ossResoucesForInterior(StorageDTO dto, HttpServletResponse response) throws Exception {
        String stsToken = getStsToken(dto);
        dto.setStsToken(stsToken);
        this.ossResouces(dto, response);
    }
}

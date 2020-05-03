package com.xfhl.common.api.service;

import com.xfhl.common.api.dto.req.StorageDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 对象存储接口
 */
public interface IStorageService {

    /**
     * @Description(描述): 上传
     * @auther: Jack Lin
     * @param :[file, dto]
     * @return :java.lang.String
     * @date: 2020/4/17 19:02
     */
    String store(MultipartFile file, StorageDTO dto);


    /**
     * @Description(描述): 删除
     * @auther: Jack Lin
     * @param :[dto]
     * @return :void
     * @date: 2020/4/17 19:02
     */
    void delete(StorageDTO dto);

    String generateUrl(StorageDTO dto);

    String getBaseUrl();
    /**
     * @Description(描述): 获取临时权限
     * @auther: Jack Lin
     * @param :[url]
     * @return :java.lang.String
     * @date: 2020/4/17 19:01
     */
    String getStsToken(StorageDTO dto) throws Exception;

    /**
     * @Description(描述): 获取oss资源
     * @auther: Jack Lin
     * @param :[url, token, response]
     * @return :void
     * @date: 2019/9/27 15:47
     */
    void ossResouces(StorageDTO dto, HttpServletResponse response) throws  Exception;

    /**
     * @Description(描述): 获取图片-内部系统用，无须验签无需登录
     * @auther: Jack Lin
     * @param :[url, response]
     * @return :void
     * @date: 2019/10/16 17:33
     */
    void ossResoucesForInterior(StorageDTO dto, HttpServletResponse response) throws  Exception;


    Object stsUploadPolicyForApp(StorageDTO dto) throws Exception;


    Object stsPostPolicy(StorageDTO dto) throws Exception;
}
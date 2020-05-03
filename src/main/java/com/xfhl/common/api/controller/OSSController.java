package com.xfhl.common.api.controller;


import com.xfhl.common.api.dto.req.StorageDTO;
import com.xfhl.common.api.enums.OSSPathEnum;
import com.xfhl.common.api.exception.CommonException;
import com.xfhl.common.api.service.IStorageService;
import com.xfhl.common.api.utils.CharUtil;
import com.xfhl.common.api.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/oss")
@Slf4j
public class OSSController {

    @Autowired
    private IStorageService aliyunStorageServiceImpl;

    private String generateKey(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        return CharUtil.getRandomString(10) + System.currentTimeMillis() + originalFilename.substring(index);
    }

    /**
     * @param :[files, mercId, mercCode, type]
     * @return :java.lang.Object
     * @Description(描述): 文件上传
     * @auther: Jack Lin
     * @date: 2020/4/21 17:01
     */
    @PostMapping("/create")
    public Object create(@RequestParam("files") MultipartFile[] files, @RequestParam("mercId") String mercId, @RequestParam("platform") String platform, @RequestParam("type") String type) {

        OSSPathEnum oneBySourceAndType = OSSPathEnum.getOneBySourceAndType(0, type);
        Optional.ofNullable(oneBySourceAndType).orElseThrow(() -> new CommonException(41004));

        StorageDTO dto = new StorageDTO();
        dto.setPlatform(platform);
        dto.setMercId(mercId);
        List<String> urls = new ArrayList<>();
        if (files != null && files.length > 0) {
            // 保存到OSS的路径前缀
            for (MultipartFile f : files) {
                String ossPath = oneBySourceAndType.getPath();
                boolean isPrivate = oneBySourceAndType.isPrivate();
                String originalFilename = f.getOriginalFilename();
                String key = platform.toLowerCase() + "/" + ossPath + generateKey(originalFilename);
                dto.setKeyName(key);
                dto.setIsPrivate(isPrivate == true ? 1 : 0);
                String urlstr = aliyunStorageServiceImpl.store(f, dto);
                urls.add(urlstr);
            }
        }
        return ResponseUtil.ok(urls);
    }


    //内部读取图片用
    @GetMapping("/interior/ossResouces")
    public void ossResoucesForInterior(@RequestParam("iconUrl") String url, @RequestParam("mercId") String mercId, @RequestParam("platform") String platform, HttpServletResponse response) throws Exception {
        StorageDTO dto = new StorageDTO();
        dto.setMercId(mercId);
        dto.setPlatform(platform);
        dto.setOssPath(url);
        aliyunStorageServiceImpl.ossResoucesForInterior(dto, response);
    }


}

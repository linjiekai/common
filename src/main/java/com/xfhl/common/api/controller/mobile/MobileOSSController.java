package com.xfhl.common.api.controller.mobile;


import cn.hutool.core.collection.CollectionUtil;
import com.xfhl.common.api.annotation.LoginUser;
import com.xfhl.common.api.controller.OSSController;
import com.xfhl.common.api.dto.req.StorageDTO;
import com.xfhl.common.api.enums.ReqResEnum;
import com.xfhl.common.api.service.IStorageService;
import com.xfhl.common.api.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/mobile/oss")
@Slf4j
public class MobileOSSController {

    @Autowired
    private OSSController ossController;
    @Autowired
    private IStorageService aliyunStorageServiceImpl;

    @PostMapping("/create")
    public Object create(@LoginUser Long userId, @RequestParam("files") MultipartFile[] files, @RequestParam("type") String type,@RequestParam("mercId") String mercId,@RequestParam("platform") String platform) {
        return ossController.create(files, mercId, platform, type);
    }

    @PostMapping("/stsToken")
    public Object getStsToken(@LoginUser Long userId,@RequestBody StorageDTO dto) throws Exception {
        Map<String, String> map = CollectionUtil.newHashMap();
        map.put("stsToken", aliyunStorageServiceImpl.getStsToken(dto));
        return ResponseUtil.ok(map);
    }

    @GetMapping("/ossResouces")
    public void ossResouces(@LoginUser Long userId,@RequestParam("stsToken") String token,@RequestParam("mercId") String mercId,@RequestParam("type") String platform, HttpServletResponse response) throws Exception {
        StorageDTO dto = new StorageDTO();
        dto.setStsToken(token);
        dto.setMercId(mercId);
        dto.setPlatform(platform);
        aliyunStorageServiceImpl.ossResouces(dto, response);
    }


    @PostMapping("/stsUploadPolicyForApp")
    public Object stsUploadPolicyForApp(@LoginUser Long userId,@RequestBody StorageDTO dto) throws Exception {
        return aliyunStorageServiceImpl.stsUploadPolicyForApp(dto);
    }

}

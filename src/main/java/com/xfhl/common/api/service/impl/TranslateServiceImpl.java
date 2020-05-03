package com.xfhl.common.api.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.xfhl.common.api.exception.CommonException;
import com.xfhl.common.api.service.ITranslateService;
import com.xfhl.common.api.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
public class TranslateServiceImpl implements ITranslateService {

    @Value("${translate.url}")
    String translateUrl;
    @Value("${translate.appCode}")
    String translateAppCode;

    @Override
    public String simpleToTraditional(String str) {
        try {

            String encode = URLEncoder.encode(str, "utf-8");
            String params = "content=" + encode + "&type=2t";
            Map<String, String> map = CollUtil.newHashMap();
            map.put("Authorization", "APPCODE " + translateAppCode);
            String s = HttpUtil.sendGet(translateUrl, params, map);
            log.info("translateUrl:{},response:{}", translateUrl, s);
            if(StrUtil.isBlank(s)){
                throw new CommonException("简体转换繁体失败");
            }
            Map<String, Object> resp = JSONUtil.toBean(s, Map.class);
            Integer status = (Integer) resp.get("status");
            if (0 == status.intValue()) {
                Map<String, Object> result = (Map<String, Object>) resp.get("result");
                String rcontent = (String) result.get("rcontent");
                return rcontent;
            }
        } catch (Exception e) {
            log.error("simpleToTraditional error:{}", e);
        }

        return str;
    }
}

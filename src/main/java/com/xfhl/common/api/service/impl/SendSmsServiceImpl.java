package com.xfhl.common.api.service.impl;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.xfhl.common.api.config.Platform;
import com.xfhl.common.api.config.SMSProperties;
import com.xfhl.common.api.dto.req.SmsDTO;
import com.xfhl.common.api.service.ISendSmservice;
import com.xfhl.common.api.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SendSmsServiceImpl implements ISendSmservice {

    public final static String CHINA_CODE = "86";// 中国国际电话区号

    @Autowired
    private IAcsClient client;
    @Autowired
    private SMSProperties smsProperties;

    @Override
    public Object sendSms(SmsDTO dto) {
        boolean isChina = false;
        String signName = "";
        try {
            if (StringUtils.isNotBlank(dto.getMobileCode()) && !CHINA_CODE.equals(dto.getMobileCode())) {
                isChina = true;
            }
            String mobile = isChina ? dto.getMobileCode() + dto.getMobile() : dto.getMobile();
            List<Platform> platforms = smsProperties.getPlatforms();
            List<Platform> collect = platforms.stream().filter((s) -> s.getMercCode().equalsIgnoreCase(dto.getPlatForm())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                signName = isChina ? collect.get(0).getAccessName() : collect.get(0).getAccessNameOverSea();
            }

            SendSmsRequest request = new SendSmsRequest();
            request.setSysConnectTimeout(5000);
            request.setSysReadTimeout(5000);
            request.setPhoneNumbers(mobile);// 待发送手机号
            request.setSignName(signName);// 短信签名
            request.setTemplateCode(dto.getTemplateId());// 短信模板
            request.setTemplateParam(dto.getJson());// 模板中的变量
            SendSmsResponse sendSmsResponse = client.getAcsResponse(request);
            log.info("短信 mobile:{},json:{},templateId:{},response:{}，RequestId:{},code:{}", mobile, dto.getJson(), dto.getTemplateId(), sendSmsResponse.getMessage(), sendSmsResponse.getRequestId(), sendSmsResponse.getCode());
            if ("OK".equalsIgnoreCase(sendSmsResponse.getCode())) {
                return ResponseUtil.ok();
            } else {
                return ResponseUtil.fail(10502, "短信发送失败!");
            }
        } catch (Exception e) {
            log.info("短信发送失败:{}, {}", dto.getMobile(), e);
            return ResponseUtil.fail();
        }
    }
}

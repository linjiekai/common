package com.xfhl.common.api.service;


import com.xfhl.common.api.dto.req.SmsDTO;

public interface ISendSmservice {

    /**
     * 发送验证码
     * @return
     */
    Object sendSms(SmsDTO dto);
}

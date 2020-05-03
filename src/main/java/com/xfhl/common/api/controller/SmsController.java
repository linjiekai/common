package com.xfhl.common.api.controller;


import com.xfhl.common.api.dto.req.SmsDTO;
import com.xfhl.common.api.service.ISendSmservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    ISendSmservice iSendSmservice;

    @PostMapping("/send")
    public Object sendSms(@RequestBody SmsDTO dto) throws Exception {
        return iSendSmservice.sendSms(dto);
    }
}

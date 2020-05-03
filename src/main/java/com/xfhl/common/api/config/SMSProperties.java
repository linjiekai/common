package com.xfhl.common.api.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "sms")
@Data
@Component
public class SMSProperties {

    private String accessKeyId;// 阿里短信AccessKeyId
    private String accessKeySecret;//accessKeySecret
    private List<Platform> platforms;

}

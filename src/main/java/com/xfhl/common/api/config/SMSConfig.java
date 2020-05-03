package com.xfhl.common.api.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SMSConfig {

    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;

    @Bean
    @Primary
    public IAcsClient iAcsClient() {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                accessKeyId,
                accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);
        return client;
    }

}

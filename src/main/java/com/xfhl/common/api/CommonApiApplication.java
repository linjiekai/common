package com.xfhl.common.api;

import com.xfhl.common.api.utils.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(scanBasePackages = {"com.xfhl.common"})
@EnableDiscoveryClient
@ServletComponentScan(basePackages = {"com.xfhl.common.api.interceptor"})
public class CommonApiApplication extends SpringBootServletInitializer {

    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CommonApiApplication.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(CommonApiApplication.class, args);
    }
}

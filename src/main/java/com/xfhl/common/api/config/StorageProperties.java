package com.xfhl.common.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "storage")
@Data
@Component
public class StorageProperties {

    private List<Platform> platforms;
    private String accessKeyId;
    private String accessKeySecret;
    private String typeImage;
    private String typeVideo;
    private String typeImageFolder;
    private String typeVideoFolder;
    private String typeOthersFolder;
    private String endpoint;
    private String privateEndpoint;

}

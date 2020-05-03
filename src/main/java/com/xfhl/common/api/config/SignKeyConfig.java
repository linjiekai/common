
package com.xfhl.common.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "sign-key")
@Data
public class SignKeyConfig {
    private Map<String, Map<String, String>> map;
}

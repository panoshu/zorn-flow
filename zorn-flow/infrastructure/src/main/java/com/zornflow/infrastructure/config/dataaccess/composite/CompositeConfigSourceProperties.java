package com.zornflow.infrastructure.config.dataaccess.composite;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/30 15:11
 **/

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "zornflow.config.composite")
public class CompositeConfigSourceProperties {
  private boolean enabled = true;
  private boolean cacheEnabled = true;
  private long cacheExpireTimeMs = 300000;
  private List<String> sourcePriorities = List.of("database", "yaml");
}

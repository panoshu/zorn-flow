package com.zornflow.gateway.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 19:41
 **/

@ConfigurationProperties(prefix = "gateway.security")
public record GlobalSecurityProperties(
  boolean enabled,
  List<String> excludePaths
) {
  public GlobalSecurityProperties {
    if (excludePaths == null) excludePaths = Collections.emptyList();
  }
}

package com.zornflow.gateway.infrastructure.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 19:42
 **/

@ConfigurationProperties(prefix = "gateway.security.replay")
@Validated
public record ReplayProperties(
  boolean enabled,
  List<String> excludePaths,
  @NotEmpty String strategy,
  Duration ttl,
  String nonceKeyPrefix
) {
  public ReplayProperties {
    if (excludePaths == null) excludePaths = Collections.emptyList();
  }
}

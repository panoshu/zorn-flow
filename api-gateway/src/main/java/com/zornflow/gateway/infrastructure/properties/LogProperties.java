package com.zornflow.gateway.infrastructure.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 19:42
 **/

@ConfigurationProperties(prefix = "gateway.security.log")
@Validated
public record LogProperties(
  boolean enabled,
  List<String> excludePaths,
  @NotEmpty String publisher,
  boolean includePayload
) {
  public LogProperties {
    if (excludePaths == null) excludePaths = Collections.emptyList();
  }
}

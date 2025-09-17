package com.zornflow.gateway.infrastructure.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 全局配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/9/16 16:59
 */

@Validated
@ConfigurationProperties(prefix = "security.global")
public record GlobalProperties(
  @NotNull boolean enabled,
  List<String> excludePaths
) {}

package com.zornflow.gateway.infrastructure.properties;

import com.zornflow.gateway.infrastructure.util.ExclusionUtils;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 19:42
 **/

@ConfigurationProperties(prefix = "gateway.security.crypto")
@Validated
public record CryptoProperties(
  boolean enabled,
  List<String> excludePaths,
  @NotEmpty String algorithmStrategy,
  @NotEmpty String keySourceStrategy,
  String keyId,
  @DataSizeUnit(DataUnit.MEGABYTES)
  DataSize maxBodySize,
  EncryptFailureStrategy onEncryptFailure,
  String secretKeyCacheName
) {
  public CryptoProperties {
    if (excludePaths == null) excludePaths = Collections.emptyList();
  }

  public enum EncryptFailureStrategy { FAIL, PASS_THROUGH }

  /**
   * 判断此模块是否应对当前请求生效。
   * @param exchange The current server web exchange.
   * @param excludePatterns 预编译的模块专属排除路径。
   * @return true 如果模块已启用且当前路径未被排除。
   */
  public boolean isApplicable(ServerWebExchange exchange, Set<PathPattern> excludePatterns) {
    return this.enabled() && !ExclusionUtils.isExcluded(exchange, excludePatterns);
  }
}

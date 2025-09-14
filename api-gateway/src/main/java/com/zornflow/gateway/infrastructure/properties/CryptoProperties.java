package com.zornflow.gateway.infrastructure.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
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
}

package com.zornflow.gateway.infrastructure.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 报文加密参数配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/9/16 17:00
 */

@Validated
@ConfigurationProperties(prefix = "security.crypto")
public record CryptoProperties(
  @NotNull boolean enabled,
  List<String> excludePaths,
  @NotEmpty String algorithmStrategy,
  @NotEmpty String keySourceStrategy,
  String keyId,
  @DataSizeUnit(DataUnit.MEGABYTES) DataSize maxBodySize,
  EncryptFailureStrategy onEncryptFailure,
  String secretKeyCacheName
) {

  public enum EncryptFailureStrategy { FAIL, PASS_THROUGH }
}

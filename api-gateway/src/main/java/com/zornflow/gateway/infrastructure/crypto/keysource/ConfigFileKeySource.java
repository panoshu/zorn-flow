package com.zornflow.gateway.infrastructure.crypto.keysource;

import com.zornflow.gateway.domain.spi.SecretKeySource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:53
 **/

@Component
@ConditionalOnProperty(name = "gateway.security.crypto.key-source-strategy", havingValue = "CONFIG_FILE")
@RequiredArgsConstructor
public class ConfigFileKeySource implements SecretKeySource {
  private final ConfigFileCryptoConfig config;

  @Override
  public Mono<String> obtainKey(String keyId) {
    return Mono.just(config.secretKey());
  }

}

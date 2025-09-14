package com.zornflow.gateway.infrastructure.crypto.keysource;

import com.zornflow.gateway.domain.spi.SecretKeySource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:58
 **/

@Component
@ConditionalOnProperty(name = "gateway.security.crypto.key-source-strategy", havingValue = "REMOTE_SERVICE")
@RequiredArgsConstructor
public class RemoteServiceKeySource implements SecretKeySource {
  private final RemoteCryptoConfig config;
  private final WebClient client = WebClient.create();

  @Override
  public Mono<String> obtainKey(String keyId) {
    return client.get()
      .uri(config.url(), keyId)
      .retrieve()
      .bodyToMono(String.class);
  }

}

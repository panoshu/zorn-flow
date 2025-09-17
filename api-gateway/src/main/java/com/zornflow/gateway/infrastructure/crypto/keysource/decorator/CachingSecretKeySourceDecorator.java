package com.zornflow.gateway.infrastructure.crypto.keysource.decorator;

import com.zornflow.gateway.domain.spi.SecretKeySource;
import com.zornflow.gateway.infrastructure.properties.CryptoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.crypto.key-source-strategy", havingValue = "REMOTE_SERVICE")
public class CachingSecretKeySourceDecorator implements SecretKeySource {

  private final SecretKeySource delegate;
  private final ReactiveRedisTemplate<String, String> redisTemplate;
  private final CryptoProperties cryptoProperties;

  @Override
  public Mono<String> obtainKey(String keyId, String version) {
    String cacheKey = buildCacheKey(keyId, version);
    return redisTemplate.opsForValue().get(cacheKey)
      .switchIfEmpty(Mono.defer(() ->
        delegate.obtainKey(keyId, version)
          .flatMap(key ->
            redisTemplate.opsForValue()
              .set(cacheKey, key, Duration.ofHours(1))
              .thenReturn(key)
          )
      ));
  }

  @Override
  public Mono<KeyDetail> obtainPrimaryKey() {
    // 主密钥也需要缓存，使用 "primary" 作为其版本标识
    String cacheKey = buildCacheKey(cryptoProperties.keyId(), "primary");

    // 缓存的是 Base64 编码的密钥字符串，我们需要 KeyDetail 对象
    // 这种场景下，我们缓存密钥 secret，然后从 delegate 获取版本号
    return delegate.obtainPrimaryKey()
      .flatMap(keyDetail -> redisTemplate.opsForValue().get(cacheKey)
        .switchIfEmpty(
          Mono.defer(() -> delegate.obtainKey(cryptoProperties.keyId(), keyDetail.version())
            .flatMap(key -> redisTemplate.opsForValue()
              .set(cacheKey, key, Duration.ofHours(1))
              .thenReturn(key)))
        )
        .map(secret -> new KeyDetail(keyDetail.version(), secret)));
  }

  private String buildCacheKey(String keyId, String version) {
    return String.join(":", cryptoProperties.secretKeyCacheName(), keyId, version);
  }
}

package com.zornflow.gateway.application;

import com.zornflow.gateway.domain.spi.CryptoEngine;
import com.zornflow.gateway.domain.spi.SecretKeySource;
import com.zornflow.gateway.infrastructure.properties.GatewaySecurityProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 加密应用服务.
 * 负责编排密钥获取和加密引擎，提供统一的加解密能力。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:03
 **/

@Service
public class CryptoService {

  private final GatewaySecurityProperties props;
  private final CryptoEngine activeEngine;
  private final SecretKeySource activeKeySource;
  private final ReactiveRedisTemplate<String, String> redisTemplate;

  public CryptoService(GatewaySecurityProperties props, List<CryptoEngine> engines, List<SecretKeySource> keySources, ReactiveRedisTemplate<String, String> redisTemplate) {
    this.props = props;
    this.redisTemplate = redisTemplate;
    Objects.requireNonNull(this.redisTemplate, "Cache 'secretKeys' is not configured.");

    // 使用策略模式，根据配置动态选择激活的引擎和密钥源
    // 将List转换为Map，便于通过名称查找
    Map<String, CryptoEngine> engineMap = engines.stream()
      .collect(Collectors.toMap(e -> e.getClass().getSimpleName().toLowerCase().replace("engine", ""), Function.identity()));
    Map<String, SecretKeySource> keySourceMap = keySources.stream()
      .collect(Collectors.toMap(ks -> ks.getClass().getSimpleName().toLowerCase().replace("keysource", ""), Function.identity()));

    this.activeEngine = engineMap.get(props.getCryptoProperties().algorithmStrategy().toLowerCase());
    this.activeKeySource = keySourceMap.get(props.getCryptoProperties().keySourceStrategy().toLowerCase());

    Objects.requireNonNull(activeEngine, "CryptoEngine not found for strategy: " + props.getCryptoProperties().algorithmStrategy());
    Objects.requireNonNull(activeKeySource, "SecretKeySource not found for strategy: " + props.getCryptoProperties().keySourceStrategy());
  }

  /**
   * 加密数据以用于传输。内部完成 AES加密 -> Base64编码。
   * @param plainBytes 明文字节
   * @return 包含Base64编码密文的字节流
   */
  public Mono<byte[]> encryptForTransport(byte[] plainBytes) {
    return getActiveKey()
      .flatMap(key -> activeEngine.encrypt(plainBytes, key))
      .map(encryptedBytes -> Base64.getEncoder().encode(encryptedBytes));
  }

  /**
   * 从传输层解密数据。内部完成 Base64解码 -> AES解密。
   * @param transportBytes 从传输层接收的字节 (应为Base64字符串的字节表示)
   * @return 解密后的明文字节
   */
  public Mono<byte[]> decryptFromTransport(byte[] transportBytes) {
    return getActiveKey()
      .flatMap(key -> {
        try {
          // 适配器层不再关心Base64，解码在这里完成
          byte[] encryptedBytes = Base64.getDecoder().decode(transportBytes);
          return activeEngine.decrypt(encryptedBytes, key);
        } catch (IllegalArgumentException e) {
          // Base64解码失败，是客户端错误
          return Mono.error(new SecurityException("Invalid Base64 format in request body", e));
        }
      });
  }

  /**
   * 获取当前激活的密钥，并应用缓存逻辑。
   *
   * @return a Mono emitting the raw key bytes.
   */
  private Mono<byte[]> getActiveKey() {
    String keyId = props.getCryptoProperties().keyId();
    String cacheName = props.getCryptoProperties().secretKeyCacheName();
    String cacheKey = cacheName + ":" + keyId;

    // 1. 先从Redis缓存中查找
    return redisTemplate.opsForValue().get(cacheKey)
      .switchIfEmpty(
        // 2. 缓存未命中，回源获取
        Mono.defer(() -> activeKeySource.obtainKey(keyId))
          .flatMap(key ->
            // 3. 放入Redis缓存并设置过期时间（例如1小时）
            redisTemplate.opsForValue()
              .set(cacheKey, key, Duration.ofHours(1))
              .thenReturn(key)
          )
      )
      .map(base64Key -> Base64.getDecoder().decode(base64Key));
  }
}

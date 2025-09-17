package com.zornflow.gateway.application;

import com.zornflow.gateway.domain.spi.CryptoEngine;
import com.zornflow.gateway.domain.spi.SecretKeySource;
import com.zornflow.gateway.infrastructure.properties.CryptoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * 加密应用服务 (最终简化版)。
 * 职责：直接使用 Spring 容器中唯一的 CryptoEngine 和 SecretKeySource 实例。
 */
@Service
@RequiredArgsConstructor
public class CryptoService {

  private final CryptoProperties props;
  private final CryptoEngine activeEngine;
  private final SecretKeySource activeKeySource; // 直接注入，不再是 List

  public record EncryptionResult(String keyVersion, byte[] encryptedBytes) {}

  public Mono<EncryptionResult> encryptForTransport(byte[] plainBytes) {
    return activeKeySource.obtainPrimaryKey() // 1. Mono<KeyDetail>
      .flatMap(keyDetail ->
        Mono.just(keyDetail.secret()) // 2. Mono<String> (密钥的Base64)
          .map(Base64.getDecoder()::decode) // 3. Mono<byte[]> (解码后的原始密钥)
          .flatMap(keyBytes -> activeEngine.encrypt(plainBytes, keyBytes)) // 4. Mono<byte[]> (加密后的密文)
          .map(Base64.getEncoder()::encode) // 5. Mono<byte[]> (Base64编码后的密文)
          .map(finalBytes -> new EncryptionResult(keyDetail.version(), finalBytes)) // 6. 封装最终结果
      );
  }


  public Mono<byte[]> decryptFromTransport(byte[] transportBytes, String keyVersion) {
    final byte[] encryptedBytes;
    try {
      encryptedBytes = Base64.getDecoder().decode(transportBytes);
    } catch (IllegalArgumentException e) {
      return Mono.error(new SecurityException("Invalid Base64 format", e));
    }

    Mono<byte[]> decryptionMono = activeKeySource.obtainKey(props.keyId(), keyVersion)
      .map(Base64.getDecoder()::decode)
      .flatMap(keyBytes -> activeEngine.decrypt(encryptedBytes, keyBytes));

    return decryptionMono.onErrorResume(e ->
      activeKeySource.obtainPrimaryKey()
        .map(keyDetail -> Base64.getDecoder().decode(keyDetail.secret()))
        .flatMap(keyBytes -> activeEngine.decrypt(encryptedBytes, keyBytes))
        .onErrorMap(fallbackError -> new SecurityException("Failed to decrypt.", fallbackError))
    );
  }
}

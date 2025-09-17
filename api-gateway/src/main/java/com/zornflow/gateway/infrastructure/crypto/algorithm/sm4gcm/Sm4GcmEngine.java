package com.zornflow.gateway.infrastructure.crypto.algorithm.sm4gcm;

import com.zornflow.gateway.domain.spi.CryptoEngine;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.Security;

/**
 * 国密 SM4/GCM/NoPadding 加密引擎实现。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/15 15:32
 **/

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "security.crypto.algorithm-strategy", havingValue = "SM4")
public final class Sm4GcmEngine implements CryptoEngine {

  private static final String ALGORITHM = "SM4/GCM/NoPadding";
  private static final String KEY_TYPE = "SM4";

  private final Sm4GcmEngineConfig config;

  // 静态代码块，用于注册 Bouncy Castle Provider
  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @Override
  public Mono<byte[]> encrypt(byte[] plainText, byte[] key) {
    return Mono.fromCallable(() -> {
      byte[] iv = new byte[this.config.ivLengthBytes()];
      new SecureRandom().nextBytes(iv);

      // 注意：需要指定 BouncyCastleProvider
      Cipher cipher = Cipher.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
      SecretKeySpec keySpec = new SecretKeySpec(key, KEY_TYPE);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(this.config.tagLengthBits(), iv);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

      byte[] cipherText = cipher.doFinal(plainText);

      return ByteBuffer.allocate(iv.length + cipherText.length)
        .put(iv)
        .put(cipherText)
        .array();
    }).subscribeOn(Schedulers.parallel());
  }

  @Override
  public Mono<byte[]> decrypt(byte[] encryptedText, byte[] key) {
    return Mono.fromCallable(() -> {
      ByteBuffer bb = ByteBuffer.wrap(encryptedText);
      byte[] iv = new byte[this.config.ivLengthBytes()];
      bb.get(iv);
      byte[] cipherText = new byte[bb.remaining()];
      bb.get(cipherText);

      Cipher cipher = Cipher.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
      SecretKeySpec keySpec = new SecretKeySpec(key, KEY_TYPE);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(this.config.tagLengthBits(), iv);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

      return cipher.doFinal(cipherText);
    }).subscribeOn(Schedulers.parallel());
  }
}

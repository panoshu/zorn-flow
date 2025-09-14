package com.zornflow.gateway.infrastructure.crypto.algorithm;

import com.zornflow.gateway.domain.spi.CryptoEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * AES/GCM/NoPadding 加密引擎的正确实现。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:14
 **/

@Component
@ConditionalOnProperty(name = "gateway.security.crypto.algorithm", havingValue = "AES/GCM/NoPadding", matchIfMissing = true)
public final class AesGcmEngine implements CryptoEngine {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_TYPE = "AES";
  private static final int GCM_TAG_LENGTH = 128; // in bits
  private static final int GCM_IV_LENGTH = 12;  // in bytes (96 bits)

  @Override
  public Mono<byte[]> encrypt(byte[] plainText, byte[] key) {
    return Mono.fromCallable(() -> {
      // 1. 生成一个每次加密都必须不同的、随机的IV
      byte[] iv = new byte[GCM_IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(key, KEY_TYPE);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

      byte[] cipherText = cipher.doFinal(plainText);

      // 2. 将 IV 和密文拼接在一起返回。这是GCM模式的标准实践。
      // 格式: [IV (12 bytes)] + [Ciphertext (variable length)]
      return ByteBuffer.allocate(iv.length + cipherText.length)
        .put(iv)
        .put(cipherText)
        .array();
    }).subscribeOn(Schedulers.parallel()); // 加密是CPU密集型操作，适合在并行线程池中执行
  }

  @Override
  public Mono<byte[]> decrypt(byte[] encryptedText, byte[] key) {
    return Mono.fromCallable(() -> {
      // 1. 从接收到的数据中分离 IV 和真正的密文
      ByteBuffer bb = ByteBuffer.wrap(encryptedText);
      byte[] iv = new byte[GCM_IV_LENGTH];
      bb.get(iv);
      byte[] cipherText = new byte[bb.remaining()];
      bb.get(cipherText);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(key, KEY_TYPE);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

      // 2. 使用分离出的IV进行解密
      return cipher.doFinal(cipherText);
    }).subscribeOn(Schedulers.parallel()); // 解密也是CPU密集型操作
  }
}

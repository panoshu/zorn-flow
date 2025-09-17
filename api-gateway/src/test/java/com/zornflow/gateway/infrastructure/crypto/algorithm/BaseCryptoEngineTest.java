package com.zornflow.gateway.infrastructure.crypto.algorithm;

import com.zornflow.gateway.domain.spi.CryptoEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import javax.crypto.AEADBadTagException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CryptoEngine 实现的抽象测试基类。
 * 提供了所有加密引擎都应通过的通用测试用例。
 */
public abstract class BaseCryptoEngineTest {

  protected CryptoEngine engine;
  protected byte[] key;
  protected byte[] wrongKey;
  protected final byte[] plainText = "这是一个用于测试的明文字符串 Zornflow Gateway".getBytes(StandardCharsets.UTF_8);

  /**
   * 由子类实现，用于提供具体的 CryptoEngine 实例和密钥。
   */
  public abstract void setUp();

  @Test
  void encryptAndDecrypt_shouldReturnOriginalPlainText() {
    // 加密然后立即解密，验证结果与原文一致
    byte[] decryptedText = engine.encrypt(plainText, key)
      .flatMap(encrypted -> engine.decrypt(encrypted, key))
      .block();

    assertThat(decryptedText).isEqualTo(plainText);
  }

  @Test
  void decryptWithWrongKey_shouldFail() {
    // 使用正确的密钥加密
    byte[] encrypted = engine.encrypt(plainText, key).block();

    // 使用错误的密钥解密，预期失败
    StepVerifier.create(engine.decrypt(encrypted, wrongKey))
      .expectError(AEADBadTagException.class) // GCM 模式下密钥错误或密文篡改会抛出此异常
      .verify();
  }

  @Test
  void decryptWithTamperedCipherText_shouldFail() {
    // 加密
    byte[] encrypted = engine.encrypt(plainText, key).block();

    // 篡改密文的最后一个字节
    Assertions.assertNotNull(encrypted);
    encrypted[encrypted.length - 1] ^= 1;

    // 使用篡改后的密文解密，预期失败
    StepVerifier.create(engine.decrypt(encrypted, key))
      .expectError(AEADBadTagException.class)
      .verify();
  }

  @Test
  void encryptTwice_shouldProduceDifferentCipherText() {
    // 连续加密两次
    byte[] encrypted1 = engine.encrypt(plainText, key).block();
    byte[] encrypted2 = engine.encrypt(plainText, key).block();

    // 由于 IV 每次都不同，两次的密文应该不相等
    assertThat(encrypted1).isNotEqualTo(encrypted2);
  }
}

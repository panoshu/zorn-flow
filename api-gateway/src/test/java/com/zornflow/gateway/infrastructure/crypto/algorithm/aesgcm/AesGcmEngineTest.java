package com.zornflow.gateway.infrastructure.crypto.algorithm.aesgcm;

import com.zornflow.gateway.infrastructure.crypto.algorithm.BaseCryptoEngineTest;
import org.junit.jupiter.api.BeforeEach;

import java.security.SecureRandom;

/**
 * AesGcmEngine 的单元测试。
 */
public class AesGcmEngineTest extends BaseCryptoEngineTest {

  @BeforeEach
  @Override
  public void setUp() {
    // 使用配置文件中的默认值来实例化 Config
    AesGcmEngineConfig config = new AesGcmEngineConfig(128, 12);
    this.engine = new AesGcmEngine(config);

    // 生成一个随机的 256位 (32字节) AES 密钥
    this.key = new byte[32];
    new SecureRandom().nextBytes(this.key);

    // 生成一个不同的错误密钥
    this.wrongKey = new byte[32];
    new SecureRandom().nextBytes(this.wrongKey);
  }
}

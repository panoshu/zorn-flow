package com.zornflow.gateway.infrastructure.crypto.algorithm.sm4gcm;

import com.zornflow.gateway.infrastructure.crypto.algorithm.BaseCryptoEngineTest;
import org.junit.jupiter.api.BeforeEach;

import java.security.SecureRandom;

/**
 * Sm4GcmEngine 的单元测试。
 */
public class Sm4GcmEngineTest extends BaseCryptoEngineTest {

  @BeforeEach
  @Override
  public void setUp() {
    // 使用配置文件中的默认值来实例化 Config
    Sm4GcmEngineConfig config = new Sm4GcmEngineConfig(128, 12);
    this.engine = new Sm4GcmEngine(config);

    // 生成一个随机的 128位 (16字节) SM4 密钥
    this.key = new byte[16];
    new SecureRandom().nextBytes(this.key);

    // 生成一个不同的错误密钥
    this.wrongKey = new byte[16];
    new SecureRandom().nextBytes(this.wrongKey);
  }
}

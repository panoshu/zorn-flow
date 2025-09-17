package com.zornflow.gateway.infrastructure.crypto.algorithm.aesgcm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AES/GCM 算法的特定配置属性。
 *
 * @param tagLengthBits GCM 认证标签的长度（以位为单位），通常为 128, 120, 112, 104 或 96。
 * @param ivLengthBytes 初始化向量（IV）的长度（以字节为单位），推荐为 12 字节（96位）。
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/15 15:10
 */
@ConfigurationProperties(prefix = "security.crypto.aes-gcm-engine")
record AesGcmEngineConfig(
  int tagLengthBits,
  int ivLengthBytes
) {}

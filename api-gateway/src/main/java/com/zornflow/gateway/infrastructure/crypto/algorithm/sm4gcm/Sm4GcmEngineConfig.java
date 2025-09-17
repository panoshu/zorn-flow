package com.zornflow.gateway.infrastructure.crypto.algorithm.sm4gcm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SM4/GCM 算法的特定配置属性。
 *
 * @param tagLengthBits GCM 认证标签的长度（以位为单位），对于SM4通常为 128。
 * @param ivLengthBytes 初始化向量（IV）的长度（以字节为单位），推荐为 12 字节（96位）。
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/15 15:30
 */
@ConfigurationProperties(prefix = "security.crypto.sm4-gcm-engine")
record Sm4GcmEngineConfig(
  int tagLengthBits,
  int ivLengthBytes
) {}

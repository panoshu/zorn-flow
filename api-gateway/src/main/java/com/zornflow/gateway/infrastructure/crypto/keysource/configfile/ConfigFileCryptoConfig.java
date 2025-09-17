package com.zornflow.gateway.infrastructure.crypto.keysource.configfile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 映射 gateway.security.crypto.config-file 配置，支持多版本密钥。
 *
 * @param keys 密钥条目列表
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.1
 * @since 2025/9/14 14:09
 **/
@ConfigurationProperties(prefix = "security.crypto.config-file")
record ConfigFileCryptoConfig(List<KeyEntry> keys) {

  /**
   * 代表单个密钥的配置条目。
   *
   * @param version 密钥的唯一版本标识符
   * @param secret Base64 编码的密钥字符串
   * @param primary 是否为当前活动的主密钥 (用于加密)
   */
  public record KeyEntry(String version, String secret, boolean primary) {}
}

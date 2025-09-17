package com.zornflow.gateway.domain.spi;

import reactor.core.publisher.Mono;

/**
 * 密钥源SPI接口 (策略接口).
 * 负责从特定来源获取加密密钥，并支持密钥轮换。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.1
 * @since 2025/9/14 13:51
 **/
public interface SecretKeySource {

  /**
   * 包含密钥版本和内容的详细信息。
   * @param version 密钥的版本标识
   * @param secret Base64编码的密钥字符串
   */
  record KeyDetail(String version, String secret) {}

  /**
   * 根据密钥ID和版本获取密钥。
   *
   * @param keyId 密钥的唯一标识符 (例如：default-key)
   * @param version 密钥的版本标识 (例如: v1, v2)。如果为null或空，应提供一个默认行为（如返回主密钥）。
   * @return Base64编码的密钥字符串
   */
  Mono<String> obtainKey(String keyId, String version);

  /**
   * 获取当前活动的主密钥及其版本，主要用于加密。
   *
   * @return 包含主密钥版本和内容的 KeyDetail 对象
   */
  Mono<KeyDetail> obtainPrimaryKey();
}

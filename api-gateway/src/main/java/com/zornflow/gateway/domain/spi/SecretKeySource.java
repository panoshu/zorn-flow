package com.zornflow.gateway.domain.spi;

import org.springframework.cache.CacheManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 密钥源SPI接口 (策略接口).
 * 负责从特定来源（如配置文件、远程服务）获取加密密钥。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:51
 **/

public interface SecretKeySource {

  /**
   * 根据密钥ID获取密钥。
   *
   * @param keyId 密钥的唯一标识符
   * @return Base64编码的密钥字符串
   */
  Mono<String> obtainKey(String keyId);
}

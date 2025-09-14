package com.zornflow.gateway.domain.spi;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 防重放缓存SPI接口 (策略接口).
 * 提供分布式的、原子的 "putIfAbsent" 能力。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:01
 **/

public interface ReplayCache {
  /**
   * 如果nonce不存在，则存入并返回true；如果已存在，则返回false。
   *
   * @param nonce 唯一随机数
   * @param ttl   缓存有效期
   * @return 如果成功存入则返回 Mono<true>，否则返回 Mono<false>
   */
  Mono<Boolean> putIfAbsent(String nonce, Duration ttl);
}

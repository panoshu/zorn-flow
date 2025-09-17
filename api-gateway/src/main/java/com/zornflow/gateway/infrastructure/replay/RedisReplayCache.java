package com.zornflow.gateway.infrastructure.replay;

import com.zornflow.gateway.domain.spi.ReplayCache;
import com.zornflow.gateway.infrastructure.properties.ReplayProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 *  基于Redis的防重放缓存实现，保证分布式环境下的原子性。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:57
 **/

@Component
@ConditionalOnProperty(name = "security.replay.strategy", havingValue = "redis")
public class RedisReplayCache implements ReplayCache {

  private final String nonceKeyPrefix;
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  public RedisReplayCache(ReplayProperties props, ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
    this.reactiveRedisTemplate = reactiveRedisTemplate;
    this.nonceKeyPrefix = props.nonceKeyPrefix();
  }

  @Override
  public Mono<Boolean> putIfAbsent(String nonce, Duration ttl) {
    // 直接使用ReactiveRedisTemplate的setIfAbsent，它会原子性地执行SETNX命令。
    // 这是保证分布式安全的正确方式，无需切换线程
    return reactiveRedisTemplate.opsForValue().setIfAbsent(this.nonceKeyPrefix + nonce, "1", ttl);
  }
}

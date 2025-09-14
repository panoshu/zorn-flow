package com.zornflow.gateway.infrastructure.cache;

import com.zornflow.gateway.domain.spi.ReplayCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:59
 **/

@Component
@RequiredArgsConstructor
public class SpringCacheReplayBridge implements ReplayCache {
  private static final String CACHE_NAME = "replayNonce";
  private final CacheManager cacheManager;

  @Override
  public Mono<Boolean> putIfAbsent(String nonce, Duration ttl) {
    return Mono.fromCallable(() -> {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
          return cache.putIfAbsent(nonce, "1") == null; // 原子
        }
        return null;
      })
      .subscribeOn(Schedulers.boundedElastic());
  }
}

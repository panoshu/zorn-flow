package com.zornflow.gateway.application;

import com.zornflow.gateway.domain.spi.ReplayCache;
import com.zornflow.gateway.infrastructure.properties.ReplayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 安全网关应用服务 (Facade).
 * 统一编排所有安全检查，如防重放。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:02
 **/

@Service
@RequiredArgsConstructor
public class ReplayProtectionService {

  private final ReplayProperties props;
  private final ReplayCache activeReplayCache;

  /**
   * 对进入的请求执行预处理检查。
   *
   * @param exchange The current server web exchange.
   * @return a Mono<Void> that completes if all checks pass, or errors out.
   */
  public Mono<Void> performPreChecks(ServerWebExchange exchange) {
    return checkReplay(exchange);
  }

  /**
   * 检查时间戳和Nonce以防止重放攻击。
   */
  private Mono<Void> checkReplay(ServerWebExchange exchange) {
    String nonce = exchange.getRequest().getHeaders().getFirst("X-Nonce");
    String timestampStr = exchange.getRequest().getHeaders().getFirst("X-Timestamp");

    if (nonce == null || timestampStr == null) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Nonce or X-Timestamp header"));
    }

    // 1. 校验时间戳是否在窗口期内
    try {
      long timestamp = Long.parseLong(timestampStr);
      long now = Instant.now().toEpochMilli();
      if (Math.abs(now - timestamp) > props.ttl().toMillis()) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Request timestamp expired"));
      }
    } catch (NumberFormatException e) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-Timestamp format"));
    }

    // 2. 校验Nonce是否已被使用
    return activeReplayCache.putIfAbsent(nonce, props.ttl())
      .flatMap(success -> {
        if (Boolean.TRUE.equals(success)) {
          return Mono.empty(); // 检查通过
        }
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Replay attack detected"));
      });
  }
}

package com.zornflow.gateway.adapter;

import com.zornflow.gateway.application.CryptoService;
import com.zornflow.gateway.application.LoggingService;
import com.zornflow.gateway.infrastructure.model.GatewayContext;
import com.zornflow.gateway.infrastructure.model.ResponseLog;
import com.zornflow.gateway.infrastructure.properties.CryptoProperties;
import com.zornflow.gateway.infrastructure.properties.SecurityRule;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 17:23
 **/

@Component
@Slf4j
public final class ResponseCryptoFilter extends AbstractSecurityFilter implements GlobalFilter, Ordered {

  private final CryptoService cryptoService;
  private final LoggingService loggingService;

  public ResponseCryptoFilter(SecurityRule securityRule, CryptoService cryptoService, LoggingService loggingService) {
    super(securityRule);
    this.cryptoService = cryptoService;
    this.loggingService = loggingService;
  }

  @Override
  protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (securityRule.shouldSkipCrypto(exchange)) {
      return chain.filter(exchange);
    }

    ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
      @Override
      @NonNull
      public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {

        if(securityRule.shouldSkipCrypto(exchange) && securityRule.shouldSkipLogging(exchange)){
          return super.writeWith(body);
        }

        return joinWithLimit(Flux.from(body), securityRule.maxBodySize())
          .defaultIfEmpty(bufferFactory().wrap(new byte[0]))
          .flatMap(joinedBuffer -> {
            byte[] plainBytes = readBytes(joinedBuffer);

            // 记录日志
            logResponse(exchange, plainBytes);

            if (securityRule.shouldSkipCrypto(exchange)) {
              return super.writeWith(Mono.just(bufferFactory().wrap(plainBytes)));
            }

            // 执行加密和失败降级
            return cryptoService.encryptForTransport(plainBytes)
              .flatMap(encryptionResult -> {
                getDelegate().getHeaders().set("X-Key-Version", encryptionResult.keyVersion());

                DataBuffer transportBuffer = bufferFactory().wrap(encryptionResult.encryptedBytes());
                return super.writeWith(Mono.just(transportBuffer));
              })
              .onErrorResume(e -> {
                if (securityRule.onEncryptFailure() == CryptoProperties.EncryptFailureStrategy.PASS_THROUGH) {
                  log.error("Failed to encrypt response, fallback to plain text as configured.", e);
                  return super.writeWith(Mono.just(bufferFactory().wrap(plainBytes)));
                }
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to encrypt response", e));
              });
          });
      }
    };
    return chain.filter(exchange.mutate().response(decorator).build());
  }

  /**
   * 异步、非阻塞地记录响应日志。
   */
  private void logResponse(ServerWebExchange exchange, byte[] plainBodyBytes) {
    if (securityRule.shouldSkipLogging(exchange)) {
      return;
    }

    Long startTime = GatewayContext.getStartTime(exchange);
    if (startTime == null) {
      return;
    }

    long durationMs = System.currentTimeMillis() - startTime;

    loggingService.logResponseAsync(ResponseLog.of(exchange, plainBodyBytes, durationMs));
  }

  @Override
  public int getOrder() {
    // 一个较低的优先级 (更接近-1)，以确保它能包裹住路由和大部分其他过滤器，从而捕获最终的响应
    return -1;
  }
}

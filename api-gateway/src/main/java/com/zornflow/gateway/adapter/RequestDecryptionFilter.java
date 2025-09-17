package com.zornflow.gateway.adapter;

import com.zornflow.gateway.application.CryptoService;
import com.zornflow.gateway.application.LoggingService;
import com.zornflow.gateway.infrastructure.model.GatewayContext;
import com.zornflow.gateway.infrastructure.model.RequestLog;
import com.zornflow.gateway.infrastructure.properties.SecurityRule;
import lombok.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
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
 * @since 2025/9/14 17:21
 **/
@Component
public final class RequestDecryptionFilter extends AbstractSecurityFilter implements GlobalFilter, Ordered {

  private final CryptoService cryptoService;
  private final LoggingService loggingService;

  public RequestDecryptionFilter(SecurityRule securityRule, CryptoService cryptoService, LoggingService loggingService) {
    super(securityRule);
    this.cryptoService = cryptoService;
    this.loggingService = loggingService;
  }

  @Override
  protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if(securityRule.shouldSkipCrypto(exchange) && securityRule.shouldSkipLogging(exchange)){
      GatewayContext.setDecryptedRequestBody(exchange, new byte[0]);
      return chain.filter(exchange);
    }

    final String keyVersion = exchange.getRequest().getHeaders().getFirst("X-Key-Version");

    return joinWithLimit(exchange.getRequest().getBody(), securityRule.maxBodySize())
      .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
      .flatMap(joinedBuffer -> {
        byte[] transportBytes = readBytes(joinedBuffer);

        if (transportBytes.length == 0 || securityRule.shouldSkipCrypto(exchange)) {
          GatewayContext.setDecryptedRequestBody(exchange, transportBytes);
          logRequest(exchange, transportBytes);
          return chain.filter(exchange);
        }

        return cryptoService.decryptFromTransport(transportBytes, keyVersion)
          .onErrorMap(e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body", e))
          .flatMap(decryptedBytes -> {
            // 解密成功后，缓存结果并立即记录日志
            GatewayContext.setDecryptedRequestBody(exchange, decryptedBytes);
            logRequest(exchange, decryptedBytes);

            // 构造可重复消费的请求
            ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
              @Override
              @NonNull
              public Flux<DataBuffer> getBody() {
                DataBuffer buf = exchange.getResponse().bufferFactory().wrap(decryptedBytes);
                return Flux.just(buf);
              }
            };
            return chain.filter(exchange.mutate().request(decorator).build());
          });
      });
  }

  private void logRequest(ServerWebExchange exchange, byte[] decryptedBytes) {
    if (securityRule.shouldSkipLogging(exchange)) {
      return;
    }
    loggingService.logRequestAsync(RequestLog.of(exchange, decryptedBytes));
  }

  @Override
  public int getOrder() {
    return -190;
  }
}

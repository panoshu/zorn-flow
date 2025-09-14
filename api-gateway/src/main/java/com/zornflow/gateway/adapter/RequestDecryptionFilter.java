package com.zornflow.gateway.adapter;

import com.zornflow.gateway.application.CryptoService;
import com.zornflow.gateway.application.LoggingService;
import com.zornflow.gateway.infrastructure.model.GatewayContext;
import com.zornflow.gateway.infrastructure.model.RequestLog;
import com.zornflow.gateway.infrastructure.properties.GatewaySecurityProperties;
import com.zornflow.gateway.infrastructure.util.DataBufferJoinUtils;
import com.zornflow.gateway.infrastructure.util.ExclusionUtils;
import lombok.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 17:21
 **/
@Component
public class RequestDecryptionFilter implements GlobalFilter, Ordered {

  private final GatewaySecurityProperties props;
  private final CryptoService cryptoService;
  private final LoggingService loggingService;
  private final Set<PathPattern> cryptoExcludePatterns;
  private final Set<PathPattern> logExcludePatterns;

  public RequestDecryptionFilter(GatewaySecurityProperties props, CryptoService cryptoService, LoggingService loggingService) {
    this.props = props;
    this.cryptoService = cryptoService;
    this.loggingService = loggingService;
    this.cryptoExcludePatterns = ExclusionUtils.compile(props.getCryptoProperties().excludePaths());
    this.logExcludePatterns = ExclusionUtils.compile(props.getLogProperties().excludePaths());
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!shouldApply(exchange)) {
      // 即使不解密，也要确保有一个空的body缓存，供下游日志过滤器等使用
      GatewayContext.setDecryptedRequestBody(exchange, new byte[0]);
      return chain.filter(exchange);
    }

    // 1. 实现有界聚合 (Bounded Aggregation)
    return DataBufferJoinUtils.joinWithLimit(exchange.getRequest().getBody(), props.getCryptoProperties().maxBodySize().toBytes())
      .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0])) // 处理空body
      .flatMap(joinedBuffer -> {
        byte[] transportBytes = readBytes(joinedBuffer);

        if (transportBytes.length == 0) {
          GatewayContext.setDecryptedRequestBody(exchange, new byte[0]);
          logRequest(exchange, new byte[0]); // 记录空body请求
          return chain.filter(exchange);
        }

        return cryptoService.decryptFromTransport(transportBytes)
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
    if (!props.getLogProperties().enabled() || ExclusionUtils.isExcluded(exchange, logExcludePatterns)) {
      return;
    }
    loggingService.logRequestAsync(RequestLog.of(exchange, decryptedBytes));
  }

  private boolean shouldApply(ServerWebExchange exchange) {
    return props.getCryptoProperties().enabled() && !ExclusionUtils.isExcluded(exchange, cryptoExcludePatterns);

  }

  private byte[] readBytes(DataBuffer dataBuffer) {
    byte[] bytes = new byte[dataBuffer.readableByteCount()];
    dataBuffer.read(bytes);
    DataBufferUtils.release(dataBuffer);
    return bytes;
  }

  @Override
  public int getOrder() {
    return -190;
  }
}

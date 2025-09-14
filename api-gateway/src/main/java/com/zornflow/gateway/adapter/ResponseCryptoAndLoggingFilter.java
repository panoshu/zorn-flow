package com.zornflow.gateway.adapter;

import com.zornflow.gateway.application.CryptoService;
import com.zornflow.gateway.application.LoggingService;
import com.zornflow.gateway.infrastructure.model.GatewayContext;
import com.zornflow.gateway.infrastructure.model.ResponseLog;
import com.zornflow.gateway.infrastructure.properties.CryptoProperties;
import com.zornflow.gateway.infrastructure.properties.GatewaySecurityProperties;
import com.zornflow.gateway.infrastructure.util.DataBufferJoinUtils;
import com.zornflow.gateway.infrastructure.util.ExclusionUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
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
 * @since 2025/9/14 17:23
 **/

@Component
@Slf4j
public class ResponseCryptoAndLoggingFilter implements GlobalFilter, Ordered {

  private final GatewaySecurityProperties props; // 注入统一的外观
  private final CryptoService cryptoService;
  private final LoggingService loggingService;
  private final Set<PathPattern> cryptoExcludePatterns;
  private final Set<PathPattern> logExcludePatterns;

  public ResponseCryptoAndLoggingFilter(GatewaySecurityProperties props, CryptoService cryptoService, LoggingService loggingService) {
    this.props = props;
    this.cryptoService = cryptoService;
    this.loggingService = loggingService;
    this.cryptoExcludePatterns = ExclusionUtils.compile(props.getCryptoProperties().excludePaths());
    this.logExcludePatterns = ExclusionUtils.compile(props.getLogProperties().excludePaths());
  }

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!props.getGlobalProperties().enabled()) {
      return chain.filter(exchange);
    }

    ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
      @Override
      @NonNull
      public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
        boolean shouldLog = props.getLogProperties().enabled() && !ExclusionUtils.isExcluded(exchange, logExcludePatterns);
        boolean shouldCrypto = props.getCryptoProperties().enabled() && !ExclusionUtils.isExcluded(exchange, cryptoExcludePatterns);

        if (!shouldLog && !shouldCrypto) {
          return super.writeWith(body);
        }

        return DataBufferJoinUtils.joinWithLimit(Flux.from(body), props.getCryptoProperties().maxBodySize().toBytes())
          .defaultIfEmpty(bufferFactory().wrap(new byte[0]))
          .flatMap(joinedBuffer -> {
            byte[] plainBytes = readBytes(joinedBuffer);

            // 记录日志
            logResponse(exchange, plainBytes);

            if (!props.getCryptoProperties().enabled()) {
              return super.writeWith(Mono.just(bufferFactory().wrap(plainBytes)));
            }

            // 执行加密和失败降级
            return cryptoService.encryptForTransport(plainBytes)
              .flatMap(transportBytes -> {
                DataBuffer buffer = bufferFactory().wrap(transportBytes);
                return super.writeWith(Mono.just(buffer));
              })
              .onErrorResume(e -> {
                if (props.getCryptoProperties().onEncryptFailure() == CryptoProperties.EncryptFailureStrategy.PASS_THROUGH) {
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
    if (!props.getLogProperties().enabled() || ExclusionUtils.isExcluded(exchange, logExcludePatterns)) {
      return;
    }

    Long startTime = GatewayContext.getStartTime(exchange);
    if (startTime == null) {
      return;
    }

    long durationMs = System.currentTimeMillis() - startTime;

    loggingService.logResponseAsync(ResponseLog.of(exchange, plainBodyBytes, durationMs));
  }

  /**
   * 辅助方法，用于从DataBuffer中安全地读取字节数组并释放内存。
   */
  private byte[] readBytes(DataBuffer dataBuffer) {
    byte[] bytes = new byte[dataBuffer.readableByteCount()];
    dataBuffer.read(bytes);
    DataBufferUtils.release(dataBuffer);
    return bytes;
  }

  @Override
  public int getOrder() {
    // 一个较低的优先级 (更接近-1)，以确保它能包裹住路由和大部分其他过滤器，从而捕获最终的响应
    return -1;
  }
}

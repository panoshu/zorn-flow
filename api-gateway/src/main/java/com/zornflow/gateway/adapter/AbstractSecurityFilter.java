package com.zornflow.gateway.adapter;

import com.zornflow.gateway.infrastructure.properties.SecurityRule;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全过滤器的抽象基类，实现了模板方法模式.
 * 它封装了通用的“是否应用过滤器”的决策逻辑。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 20:29
 **/

public sealed abstract class AbstractSecurityFilter implements GlobalFilter
permits ReplayProtectionFilter, RequestDecryptionFilter, ResponseCryptoFilter {

  protected final SecurityRule securityRule;

  protected AbstractSecurityFilter(SecurityRule globalProperties) {
    this.securityRule = globalProperties;
  }

  /**
   * 过滤器主模板方法。
   * 封装了全局开关和全局白名单的通用判断逻辑。
   */
  @Override
  public final Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 首先执行全局检查
    if (securityRule.isGlobalSecurityDisable(exchange)) {
      return chain.filter(exchange);
    }
    // 调用由子类实现的具体过滤逻辑
    return doFilter(exchange, chain);
  }


  /**
   * 抽象方法，由具体的过滤器子类实现其核心业务逻辑。
   * @param exchange The current server web exchange.
   * @param chain The gateway filter chain.
   * @return A Mono<Void> to indicate completion.
   */
  protected abstract Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain);

  /**
   * 安全版 join：限制总大小，避免内存攻击，自动处理超限释放和异常释放。
   *
   * @param source Flux<DataBuffer>
   * @param limitBytes 最大允许的字节数
   * @return Mono<DataBuffer>（已合并）
   */
  protected Mono<DataBuffer> joinWithLimit(Flux<DataBuffer> source, long limitBytes) {
    AtomicLong currentSize = new AtomicLong();

    Flux<DataBuffer> guarded = source.handle((buffer, sink) -> {
      try {
        long newSize = currentSize.addAndGet(buffer.readableByteCount());
        if (newSize > limitBytes) {
          // 超限 → 自己消费，自己释放
          DataBufferUtils.release(buffer);
          sink.error(new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
            "Payload too large: " + newSize + " > limit " + limitBytes));
        } else {
          // 正常 → 交给 join，join 会负责释放
          sink.next(buffer);
        }
      } catch (Throwable e) {
        // 异常 → 必须释放当前 buffer，否则泄漏
        DataBufferUtils.release(buffer);
        sink.error(e);
      }
    });

    // join 会在正常和 onError 路径里释放由它接管的 buffer
    return DataBufferUtils.join(guarded);
  }

  /**
   * 辅助方法，用于从DataBuffer中安全地读取字节数组并释放内存。
   */
  protected byte[] readBytes(DataBuffer dataBuffer) {
    byte[] bytes = new byte[dataBuffer.readableByteCount()];
    dataBuffer.read(bytes);
    DataBufferUtils.release(dataBuffer);
    return bytes;
  }

}

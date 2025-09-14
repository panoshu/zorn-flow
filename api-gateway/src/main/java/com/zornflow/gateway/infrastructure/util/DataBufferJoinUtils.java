package com.zornflow.gateway.infrastructure.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 18:38
 **/
public final class DataBufferJoinUtils {

  private DataBufferJoinUtils() {}

  /**
   * 安全版 join：限制总大小，避免内存攻击，自动处理超限释放和异常释放。
   *
   * @param source Flux<DataBuffer>
   * @param limitBytes 最大允许的字节数
   * @return Mono<DataBuffer>（已合并）
   */
  public static Mono<DataBuffer> joinWithLimit(Flux<DataBuffer> source, long limitBytes) {
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
}

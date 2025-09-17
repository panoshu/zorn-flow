package com.zornflow.gateway.infrastructure.log;

import com.zornflow.gateway.infrastructure.model.RequestLog;
import com.zornflow.gateway.infrastructure.model.ResponseLog;
import com.zornflow.gateway.domain.spi.LogPublisher;
import com.zornflow.gateway.infrastructure.properties.LoggingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 输出日志到磁盘文件
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 15:33
 **/

@Slf4j
@Component
@ConditionalOnProperty(name = "security.log.publisher", havingValue = "file")
@RequiredArgsConstructor
public class FileLogPublisher implements LogPublisher {

  private final LoggingProperties props;

  @Override
  public Mono<Void> publishRequest(RequestLog requestLog) {
    return Mono.fromRunnable(() -> {
      String payload = props.includePayload() ? requestLog.decryptedRequestBodyPreview() : "[REDACTED]";

      // 使用SLF4J的参数化日志，性能好且安全
      // 我们手动构建一个JSON格式的字符串作为日志消息体
      log.info("GatewayLog type=\"request\" requestId=\"{}\" method=\"{}\" path=\"{}\" payload=\"{}\"",
        requestLog.requestId(),
        requestLog.method(),
        requestLog.path(),
        payload != null ? payload.replace("\"", "\\\"") : "" // 对payload中的引号进行简单转义
      );
    });
  }

  @Override
  public Mono<Void> publishResponse(ResponseLog responseLog) {
    return Mono.fromRunnable(() -> {
      String payload = props.includePayload() ? responseLog.plainResponseBodyPreview() : "[REDACTED]";
      Integer status = responseLog.status() != null ? responseLog.status().value() : null;

      log.info("GatewayLog type=\"response\" requestId=\"{}\" status=\"{}\" durationMs=\"{}\" payload=\"{}\"",
        responseLog.requestId(),
        status,
        responseLog.durationMs(),
        payload != null ? payload.replace("\"", "\\\"") : ""
      );
    });
  }
}

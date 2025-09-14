package com.zornflow.gateway.infrastructure.model;

import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 16:11
 **/

@Builder
public record ResponseLog(
  String requestId,
  Instant responseTime,
  HttpStatusCode status,
  HttpHeaders responseHeaders,
  String plainResponseBodyPreview,
  String plainResponseBodyMd5,
  long durationMs
) {
  public static ResponseLog of(ServerWebExchange exchange, byte[] plainBodyBytes, long durationMs) {
    // 只取前 1KB + MD5
    int headLen = Math.min(plainBodyBytes.length, 512);
    String head = new String(plainBodyBytes, 0, headLen);
    String md5 = DigestUtils.md5DigestAsHex(plainBodyBytes);
    return ResponseLog.builder()
      .requestId(exchange.getRequest().getId())
      .responseTime(Instant.now())
      .status(exchange.getResponse().getStatusCode())
      .responseHeaders(exchange.getResponse().getHeaders())
      .plainResponseBodyPreview(head)
      .plainResponseBodyMd5(md5)
      .durationMs(durationMs)
      .build();
  }

}

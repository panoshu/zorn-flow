package com.zornflow.gateway.infrastructure.model;

import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
public record RequestLog(
  String requestId,
  Instant requestTime,
  HttpMethod method,
  String path,
  HttpHeaders requestHeaders,
  String decryptedRequestBodyPreview,
  String decryptedRequestBodyMd5
) {

  public static RequestLog of(ServerWebExchange exchange, byte[]  decryptedBytes){
    // 只取前 1KB + MD5
    int headLen = Math.min(decryptedBytes.length, 512);
    String head = new String(decryptedBytes, 0, headLen);
    String md5 = DigestUtils.md5DigestAsHex(decryptedBytes);
    return RequestLog.builder()
      .requestId(exchange.getRequest().getId())
      .requestTime(Instant.now())
      .method(exchange.getRequest().getMethod())
      .path(exchange.getRequest().getPath().value())
      .requestHeaders(exchange.getRequest().getHeaders())
      .decryptedRequestBodyPreview(head)
      .decryptedRequestBodyMd5(md5)
      .build();
  }
}

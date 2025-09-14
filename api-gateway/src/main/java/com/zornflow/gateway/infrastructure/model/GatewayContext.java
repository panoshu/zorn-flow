package com.zornflow.gateway.infrastructure.model;

import org.springframework.web.server.ServerWebExchange;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 17:18
 **/

public final class GatewayContext {

  private static final String START_TIME_ATTR = "gateway.startTime";
  private static final String CACHED_DECRYPTED_REQUEST_BODY_ATTR = "gateway.cachedDecryptedRequestBody";
  private static final String CACHED_PLAIN_RESPONSE_BODY_ATTR = "gateway.cachedPlainResponseBody";

  private GatewayContext() {}

  public static void setStartTime(ServerWebExchange exchange) {
    exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());
  }

  public static Long getStartTime(ServerWebExchange exchange) {
    return exchange.getAttribute(START_TIME_ATTR);
  }

  public static void setDecryptedRequestBody(ServerWebExchange exchange, byte[] body) {
    exchange.getAttributes().put(CACHED_DECRYPTED_REQUEST_BODY_ATTR, body);
  }

  public static byte[] getDecryptedRequestBody(ServerWebExchange exchange) {
    return exchange.getAttribute(CACHED_DECRYPTED_REQUEST_BODY_ATTR);
  }

  public static void setPlainResponseBody(ServerWebExchange exchange, byte[] body) {
    exchange.getAttributes().put(CACHED_PLAIN_RESPONSE_BODY_ATTR, body);
  }

  public static byte[] getPlainResponseBody(ServerWebExchange exchange) {
    return exchange.getAttribute(CACHED_PLAIN_RESPONSE_BODY_ATTR);
  }
}

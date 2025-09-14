package com.zornflow.gateway.infrastructure.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:48
 **/

@Getter
@Component
@RequiredArgsConstructor
public final class GatewaySecurityProperties{
  private final GlobalSecurityProperties globalProperties;
  private final CryptoProperties cryptoProperties;
  private final ReplayProperties replayProperties;
  private final LogProperties logProperties;
}

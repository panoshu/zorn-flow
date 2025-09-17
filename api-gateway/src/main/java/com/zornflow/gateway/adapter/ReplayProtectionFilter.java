package com.zornflow.gateway.adapter;

import com.zornflow.gateway.application.ReplayProtectionService;
import com.zornflow.gateway.infrastructure.model.GatewayContext;
import com.zornflow.gateway.infrastructure.properties.SecurityRule;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 17:19
 **/

@Component
public final class ReplayProtectionFilter extends AbstractSecurityFilter implements Ordered {

  private final ReplayProtectionService securityService;

  public ReplayProtectionFilter(SecurityRule securityRule, ReplayProtectionService securityService) {
    super(securityRule); // 调用父类构造函数
    this.securityService = securityService;
  }

  @Override
  protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GatewayContext.setStartTime(exchange);

    Mono<Void> replayCheckMono = Mono.empty();

    if (securityRule.shouldApplyReplayProtection(exchange)) {
      replayCheckMono = securityService.performPreChecks(exchange);
    }

    return replayCheckMono.then(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    return -200;
  }
}

package com.zornflow.gateway.adapter;

import com.zornflow.gateway.application.SecurityGatewayService;
import com.zornflow.gateway.infrastructure.model.GatewayContext;
import com.zornflow.gateway.infrastructure.properties.GatewaySecurityProperties;
import com.zornflow.gateway.infrastructure.util.ExclusionUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 17:19
 **/

@Component
public class ExclusionAndPreCheckFilter implements GlobalFilter, Ordered {

  private final GatewaySecurityProperties props; // 注入统一的外观
  private final SecurityGatewayService securityService;
  private final Set<PathPattern> globalExcludePatterns;
  private final Set<PathPattern> replayExcludePatterns;

  public ExclusionAndPreCheckFilter(GatewaySecurityProperties props, SecurityGatewayService securityService) {
    this.props = props;
    this.securityService = securityService;
    this.globalExcludePatterns = ExclusionUtils.compile(props.getGlobalProperties().excludePaths());
    this.replayExcludePatterns = ExclusionUtils.compile(props.getReplayProperties().excludePaths());
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!props.getGlobalProperties().enabled() || ExclusionUtils.isExcluded(exchange, globalExcludePatterns)) {
      return chain.filter(exchange);
    }

    GatewayContext.setStartTime(exchange);

    return Mono.just(exchange)
      .map(this::checkPayloadSize)
      .flatMap(securityService::performPreChecks)
      .then(chain.filter(exchange));
  }

  private ServerWebExchange checkPayloadSize(ServerWebExchange exchange) {
    long contentLength = exchange.getRequest().getHeaders().getContentLength();
    if (contentLength > props.getCryptoProperties().maxBodySize().toBytes()) {
      throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
    }
    return exchange;
  }

  @Override
  public int getOrder() {
    return -200; // 最高的业务优先级
  }
}

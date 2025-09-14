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
public class ExclusionAndPreCheckFilter extends AbstractSecurityFilter implements Ordered { // 继承基类

  private final GatewaySecurityProperties props;
  private final SecurityGatewayService securityService;
  private final Set<PathPattern> replayExcludePatterns;

  public ExclusionAndPreCheckFilter(GatewaySecurityProperties props, SecurityGatewayService securityService) {
    super(props.getGlobalProperties()); // 调用父类构造函数
    this.props = props;
    this.securityService = securityService;
    this.replayExcludePatterns = ExclusionUtils.compile(props.getReplayProperties().excludePaths());
  }

  @Override
  protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GatewayContext.setStartTime(exchange);

    Mono<Void> replayCheckMono = Mono.empty();
    // 使用模块配置自己的 isApplicable 方法
    if (props.getReplayProperties().isApplicable(exchange, replayExcludePatterns)) {
      replayCheckMono = securityService.performPreChecks(exchange);
    }

    return replayCheckMono.then(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    return -200;
  }
}

package com.zornflow.gateway.adapter;

import com.zornflow.gateway.infrastructure.properties.GlobalSecurityProperties;
import com.zornflow.gateway.infrastructure.util.ExclusionUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 安全过滤器的抽象基类，实现了模板方法模式.
 * 它封装了通用的“是否应用过滤器”的决策逻辑。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 20:29
 **/

public abstract class AbstractSecurityFilter implements GlobalFilter {

  private final GlobalSecurityProperties globalProperties;
  private final Set<PathPattern> globalExcludePatterns;

  protected AbstractSecurityFilter(GlobalSecurityProperties globalProperties) {
    this.globalProperties = globalProperties;
    this.globalExcludePatterns = ExclusionUtils.compile(globalProperties.excludePaths());
  }

  /**
   * 过滤器主模板方法。
   * 封装了全局开关和全局白名单的通用判断逻辑。
   */
  @Override
  public final Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 首先执行全局检查
    if (!globalProperties.enabled() || ExclusionUtils.isExcluded(exchange, globalExcludePatterns)) {
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
}

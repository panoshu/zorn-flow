package com.zornflow.gateway.adapter;

import com.zornflow.gateway.infrastructure.properties.GatewaySecurityProperties;
import com.zornflow.gateway.infrastructure.util.ExclusionUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
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

public abstract class AbstractGatewaySecurityFilter implements GlobalFilter, Ordered {

  protected final GatewaySecurityProperties props;
  private final Set<PathPattern> globalExcludePatterns;

  protected AbstractGatewaySecurityFilter(GatewaySecurityProperties props) {
    this.props = props;
    this.globalExcludePatterns = ExclusionUtils.compile(props.getGlobalProperties().excludePaths());
  }

  @Override
  @NonNull
  public final Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 模板方法：定义了所有安全过滤器的执行骨架
    if (isGloballyDisabled(exchange) || !shouldApply(exchange)) {
      // 如果全局禁用，或当前过滤器不适用，则直接跳过
      return chain.filter(exchange);
    }
    // 调用子类实现的具体过滤逻辑
    return applyFilter(exchange, chain);
  }

  /**
   * 检查全局开关和全局白名单。
   */
  private boolean isGloballyDisabled(ServerWebExchange exchange) {
    return !props.getGlobalProperties().enabled() || ExclusionUtils.isExcluded(exchange, globalExcludePatterns);
  }

  /**
   * 【钩子方法】由子类实现，用于判断当前过滤器是否应该被应用。
   * (e.g., 检查模块开关和模块专属白名单)
   *
   * @param exchange the current server web exchange
   * @return true if the filter should be applied, false otherwise
   */
  protected abstract boolean shouldApply(ServerWebExchange exchange);

  /**
   * 【抽象方法】由子类实现，包含具体的过滤逻辑。
   *
   * @param exchange the current server web exchange
   * @param chain provides a way to delegate to the next filter
   * @return a Mono<Void> to indicate when request processing is complete
   */
  protected abstract Mono<Void> applyFilter(ServerWebExchange exchange, GatewayFilterChain chain);
}

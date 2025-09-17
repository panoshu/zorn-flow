package com.zornflow.gateway.infrastructure.properties;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 安全功能参数配置解析器 带缓存
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/9/16 16:47
 */

public final class SecurityRuleParser {
  private static final PathPatternParser PARSER = new PathPatternParser();

  private final ConcurrentHashMap<List<String>, Set<PathPattern>> CACHE = new ConcurrentHashMap<>();

  public boolean isExcluded(ServerWebExchange exchange, List<String> patterns) {
    if (patterns == null || patterns.isEmpty()) {
      return false;
    }

    Set<PathPattern> pathPatterns = CACHE.computeIfAbsent(
      patterns,
      p -> p.stream()
        .filter(s -> s != null && !s.trim().isEmpty())
        .map(PARSER::parse)
        .collect(Collectors.toSet())
    );

    return pathPatterns.stream()
      .anyMatch(p -> p.matches(exchange.getRequest().getPath().pathWithinApplication()));
  }

  /**
   * 判断配置是否适用于当前请求
   */
  public boolean isApplicable(boolean enabled, List<String> excludePaths, ServerWebExchange exchange) {
    return enabled && !isExcluded(exchange, excludePaths);
  }

  /** 刷新入口：配置变更后清空缓存 */
  public void refresh() {
    CACHE.clear();
  }
}

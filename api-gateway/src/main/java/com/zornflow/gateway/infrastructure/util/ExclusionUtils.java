package com.zornflow.gateway.infrastructure.util;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 19:43
 **/

public final class ExclusionUtils {

  private ExclusionUtils() {}

  public static Set<PathPattern> compile(List<String> patterns) {
    PathPatternParser parser = new PathPatternParser();
    return patterns.stream().map(parser::parse).collect(Collectors.toSet());
  }

  public static boolean isExcluded(ServerWebExchange exchange, Set<PathPattern> patterns) {
    return patterns.stream()
      .anyMatch(p -> p.matches(exchange.getRequest().getPath().pathWithinApplication()));
  }
}

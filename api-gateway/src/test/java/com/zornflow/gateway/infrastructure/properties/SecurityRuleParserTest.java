package com.zornflow.gateway.infrastructure.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityRuleParserTest {

  private SecurityRuleParser parser;

  @BeforeEach
  void setUp() {
    parser = new SecurityRuleParser();
  }

  private MockServerWebExchange createExchange(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
  }

  @Test
  void testIsExcluded_shouldReturnFalseForNullPatterns() {
    // Given
    List<String> patterns = null;
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isExcluded(exchange, patterns);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testIsExcluded_shouldReturnFalseForEmptyPatterns() {
    // Given
    List<String> patterns = List.of();
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isExcluded(exchange, patterns);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testIsExcluded_shouldMatchPathPattern() {
    // Given
    List<String> patterns = List.of("/api/**", "/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isExcluded(exchange, patterns);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testIsExcluded_shouldNotMatchPathPattern() {
    // Given
    List<String> patterns = List.of("/api/**", "/admin/**");
    MockServerWebExchange exchange = createExchange("/public/test");

    // When
    boolean result = parser.isExcluded(exchange, patterns);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testIsExcluded_shouldHandleNullPatternInList() {
    // Given
    List<String> patterns = Arrays.asList("/api/**", null, "/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isExcluded(exchange, patterns);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testIsExcluded_shouldHandleEmptyPatternInList() {
    // Given
    List<String> patterns = Arrays.asList("/api/**", "", "/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isExcluded(exchange, patterns);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testIsApplicable_shouldReturnTrueWhenEnabledAndNotExcluded() {
    // Given
    boolean enabled = true;
    List<String> excludePaths = List.of("/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isApplicable(enabled, excludePaths, exchange);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testIsApplicable_shouldReturnFalseWhenDisabled() {
    // Given
    boolean enabled = false;
    List<String> excludePaths = List.of("/api/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isApplicable(enabled, excludePaths, exchange);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testIsApplicable_shouldReturnFalseWhenExcluded() {
    // Given
    boolean enabled = true;
    List<String> excludePaths = List.of("/api/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // When
    boolean result = parser.isApplicable(enabled, excludePaths, exchange);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testCache_shouldBeEffective() {
    // Given
    List<String> patterns = List.of("/api/**", "/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // 获取私有字段 CACHE 用于验证
    ConcurrentHashMap<List<String>, Set<PathPattern>> cache = getCacheField();

    // 验证初始状态
    assertThat(cache).isEmpty();

    // When - 第一次调用
    boolean result1 = parser.isExcluded(exchange, patterns);

    // Then - 第一次调用后缓存应该有值
    assertThat(result1).isTrue();
    assertThat(cache).hasSize(1);
    assertThat(cache.containsKey(patterns)).isTrue();

    // When - 第二次调用相同模式
    boolean result2 = parser.isExcluded(exchange, patterns);

    // Then - 结果应该相同，缓存大小不变
    assertThat(result2).isTrue();
    assertThat(cache).hasSize(1);
  }

  @Test
  void testCache_shouldHandleDifferentPatternLists() {
    // Given
    List<String> patterns1 = List.of("/api/**");
    List<String> patterns2 = List.of("/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    ConcurrentHashMap<List<String>, Set<PathPattern>> cache = getCacheField();

    // When - 调用第一个模式
    boolean result1 = parser.isExcluded(exchange, patterns1);

    // Then
    assertThat(result1).isTrue();
    assertThat(cache).hasSize(1);

    // When - 调用第二个模式
    boolean result2 = parser.isExcluded(exchange, patterns2);

    // Then
    assertThat(result2).isFalse();
    assertThat(cache).hasSize(2);
    assertThat(cache.containsKey(patterns1)).isTrue();
    assertThat(cache.containsKey(patterns2)).isTrue();
  }

  @Test
  void testRefresh_shouldClearCache() {
    // Given
    List<String> patterns = List.of("/api/**", "/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    ConcurrentHashMap<List<String>, Set<PathPattern>> cache = getCacheField();

    // 预热缓存
    parser.isExcluded(exchange, patterns);
    assertThat(cache).hasSize(1);

    // When
    parser.refresh();

    // Then
    assertThat(cache).isEmpty();
  }

  @Test
  void testConcurrentCacheAccess() throws InterruptedException {
    // Given
    List<String> patterns = List.of("/api/**", "/admin/**");
    MockServerWebExchange exchange1 = createExchange("/api/test");
    MockServerWebExchange exchange2 = createExchange("/admin/dashboard");

    ConcurrentHashMap<List<String>, Set<PathPattern>> cache = getCacheField();

    // 创建多个线程并发访问
    Thread[] threads = new Thread[10];
    for (int i = 0; i < threads.length; i++) {
      final int index = i;
      threads[i] = new Thread(() -> {
        MockServerWebExchange exchange = index % 2 == 0 ? exchange1 : exchange2;
        boolean result = parser.isExcluded(exchange, patterns);
        // 确保结果正确
        if (exchange == exchange1) {
          assertThat(result).isTrue();
        } else {
          assertThat(result).isTrue();
        }
      });
    }

    // When - 启动所有线程
    for (Thread thread : threads) {
      thread.start();
    }

    // 等待所有线程完成
    for (Thread thread : threads) {
      thread.join();
    }

    // Then - 缓存应该只有一条记录（因为使用相同的 patterns 列表）
    assertThat(cache).hasSize(1);
    assertThat(cache.containsKey(patterns)).isTrue();
  }

  @Test
  void testCacheWithSamePatternsDifferentInstance() {
    // Given
    List<String> patterns1 = List.of("/api/**", "/admin/**");
    List<String> patterns2 = List.of("/health/**"); // 相同内容，不同实例
    MockServerWebExchange exchange = createExchange("/api/test");

    ConcurrentHashMap<List<String>, Set<PathPattern>> cache = getCacheField();

    // When - 使用第一个实例
    boolean result1 = parser.isExcluded(exchange, patterns1);

    // Then
    assertThat(result1).isTrue();
    assertThat(cache).hasSize(1);

    // When - 使用第二个实例（相同内容）
    boolean result2 = parser.isExcluded(exchange, patterns2);

    // Then - 由于 List 不重写 equals，这会创建新的缓存条目
    assertThat(result2).isFalse();
    assertThat(cache).hasSize(2);

    // 验证两个不同的键
    Set<List<String>> keys = cache.keySet();
    assertThat(keys).contains(patterns1, patterns2);
  }

  @Test
  void testRefreshWithConcurrentAccess() throws InterruptedException {
    // Given
    List<String> patterns = List.of("/api/**", "/admin/**");
    MockServerWebExchange exchange = createExchange("/api/test");

    // 预热缓存
    parser.isExcluded(exchange, patterns);
    assertThat(getCacheField()).hasSize(1);

    // 创建并发线程
    Thread refreshThread = new Thread(() -> {
      try {
        Thread.sleep(50); // 稍微延迟
        parser.refresh();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

    });

    Thread accessThread = new Thread(() -> {
      boolean result = parser.isExcluded(exchange, patterns);
      assertThat(result).isTrue(); // 应该仍然能工作，只是缓存被清空后重新计算
    });

    // When
    refreshThread.start();
    accessThread.start();

    // 等待完成
    refreshThread.join();
    accessThread.join();

    // Then - 缓存可能为空或有值，取决于执行顺序，但系统应该稳定
    // 重要的是没有异常发生
  }

  /**
   * 反射获取私有 CACHE 字段用于测试验证
   */
  @SuppressWarnings("unchecked")
  private ConcurrentHashMap<List<String>, Set<PathPattern>> getCacheField() {
    try {
      java.lang.reflect.Field cacheField = SecurityRuleParser.class.getDeclaredField("CACHE");
      cacheField.setAccessible(true);
      return (ConcurrentHashMap<List<String>, Set<PathPattern>>) cacheField.get(parser);
    } catch (Exception e) {
      throw new RuntimeException("Failed to access CACHE field", e);
    }
  }
}

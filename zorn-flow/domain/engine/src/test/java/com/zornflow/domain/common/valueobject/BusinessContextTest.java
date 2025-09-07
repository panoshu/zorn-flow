package com.zornflow.domain.common.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@DisplayName("BusinessContext 值对象单元测试")
class BusinessContextTest {

  @Test
  @DisplayName("with: 应返回一个新的 BusinessContext 实例，而不是修改原实例")
  void with_shouldReturnNewInstance() {
    // Arrange
    BusinessContext originalContext = new BusinessContext(Map.of("key1", "value1"));

    // Act
    BusinessContext newContext = originalContext.with("key2", "value2");

    // Assert
    assertNotSame(originalContext, newContext, "with() 方法必须返回一个新实例");
    assertThat(originalContext.data()).containsExactly(Map.entry("key1", "value1"));
    assertThat(newContext.data()).containsExactlyInAnyOrderEntriesOf(Map.of("key1", "value1", "key2", "value2"));
  }

  @Test
  @DisplayName("with: 当覆盖现有键时，应返回一个更新了值的新实例")
  void with_shouldOverwriteExistingKeyInNewInstance() {
    // Arrange
    BusinessContext originalContext = new BusinessContext(Map.of("key1", "value1"));

    // Act
    BusinessContext newContext = originalContext.with("key1", "newValue");

    // Assert
    assertNotSame(originalContext, newContext);
    assertThat(originalContext.data()).containsEntry("key1", "value1");
    assertThat(newContext.data()).containsEntry("key1", "newValue");
  }

  @Test
  @DisplayName("merge: 应能合并另一个 map 并返回一个新实例")
  void merge_shouldMergeMapAndReturnNewInstance() {
    // Arrange
    BusinessContext originalContext = new BusinessContext(Map.of("key1", "value1", "key2", "original"));
    Map<String, Object> toMerge = Map.of("key2", "merged", "key3", 123);

    // Act
    BusinessContext newContext = originalContext.merge(toMerge);

    // Assert
    assertNotSame(originalContext, newContext);
    assertThat(originalContext.data()).containsExactlyInAnyOrderEntriesOf(Map.of("key1", "value1", "key2", "original"));
    assertThat(newContext.data()).containsExactlyInAnyOrderEntriesOf(Map.of("key1", "value1", "key2", "merged", "key3", 123));
  }

  @Test
  @DisplayName("get: 应能安全地获取并转换指定类型的值")
  void get_shouldRetrieveAndCastValue() {
    // Arrange
    BusinessContext context = new BusinessContext(Map.of("name", "zornflow", "count", 100));

    // Act
    String name = context.get("name", String.class);
    Integer count = context.get("count", Integer.class);

    // Assert
    assertThat(name).isEqualTo("zornflow");
    assertThat(count).isEqualTo(100);
  }
}

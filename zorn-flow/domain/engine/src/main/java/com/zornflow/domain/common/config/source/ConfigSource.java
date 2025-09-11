package com.zornflow.domain.common.config.source;

import com.zornflow.domain.common.config.model.ModelConfig;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 3:35
 */
public sealed interface ConfigSource<T extends ModelConfig> permits ReadableConfigSource, WriteableConfigSource {

  /**
   * 获取配置源名称
   *
   * @return 配置源名称
   */
  default String getSourceName() {
    return this.getClass().getSimpleName();
  }

  /**
   * 获取配置源类型
   *
   * @return 配置源类型
   */
  ConfigSourceType getSourceType();

  boolean available();

  /**
   * 配置源类型枚举
   */
  enum ConfigSourceType {
    YAML, DATABASE, PROPERTIES, REMOTE, COMPOSITE, CACHED_COMPOSITE
  }
}

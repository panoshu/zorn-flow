package com.zornflow.domain.common.config.source;

import com.zornflow.domain.common.config.model.ModelConfig;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 3:48
 */
public non-sealed interface WriteableConfigSource<T extends ModelConfig> extends ConfigSource<T> {
  void save(T modelConfig);        // 仅可写源实现
  void delete(String id);
}

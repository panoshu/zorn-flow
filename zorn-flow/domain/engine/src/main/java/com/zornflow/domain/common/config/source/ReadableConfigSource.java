package com.zornflow.domain.common.config.source;

import com.zornflow.domain.common.config.model.ModelConfig;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 3:36
 */
public non-sealed interface ReadableConfigSource<T extends ModelConfig> extends ConfigSource<T>{
  Optional<T> load(String id) throws IOException;
  Map<String, T> loadAll() throws IOException;
}

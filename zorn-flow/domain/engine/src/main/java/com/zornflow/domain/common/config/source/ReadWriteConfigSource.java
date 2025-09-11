package com.zornflow.domain.common.config.source;

import com.zornflow.domain.common.config.model.ModelConfig;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 3:49
 */
public interface ReadWriteConfigSource<T extends ModelConfig> extends ReadableConfigSource<T>, WriteableConfigSource<T> {
}

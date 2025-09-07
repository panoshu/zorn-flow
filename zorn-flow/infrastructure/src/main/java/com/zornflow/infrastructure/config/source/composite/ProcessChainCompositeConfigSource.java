package com.zornflow.infrastructure.config.source.composite;

import com.zornflow.domain.common.config.source.ConfigSource;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProcessChainConfig 的组合数据源实现。
 * 它会从Spring容器中自动注入所有 ProcessChainConfig 类型的 ConfigSource Bean，
 * 并根据 @Order 注解进行排序。
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 16:05
 **/

@Service
public final class ProcessChainCompositeConfigSource extends AbstractCompositeConfigSource<ProcessChainConfig> {

  public ProcessChainCompositeConfigSource(List<ConfigSource<ProcessChainConfig>> sources) {
    super(sources);
  }

}

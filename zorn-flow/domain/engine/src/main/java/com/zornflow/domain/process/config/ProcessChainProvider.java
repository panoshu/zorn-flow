package com.zornflow.domain.process.config;

import com.zornflow.domain.process.entity.ProcessChain;

import java.util.Collection;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/26 07:27
 **/

public interface ProcessChainProvider {
  Collection<ProcessChain> loadProcessDefinitions();
  String getSourceName();
}

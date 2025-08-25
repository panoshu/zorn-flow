package com.zornflow.domain.rule.config;

import com.zornflow.domain.rule.entity.RuleChain;

import java.util.Collection;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/26 07:27
 **/

public interface RuleChainProvider {
  Collection<RuleChain> loadRuleChainDefinitions();
  String getSourceName();
}

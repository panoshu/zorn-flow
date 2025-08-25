package com.zornflow.domain.rule.service;

import com.zornflow.domain.rule.config.RuleChainConfig;
import com.zornflow.domain.rule.types.RuleChainId;

import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/1 12:23
 */
public interface ConfigManagementService {
  Optional<RuleChainConfig> getRuleChainConfig(RuleChainId ruleChainId);
}

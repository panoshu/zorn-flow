package com.zornflow.domain.rule.service;

import com.zornflow.domain.common.context.FlowContext;
import com.zornflow.domain.common.types.BusinessContext;
import com.zornflow.domain.rule.entity.RuleChain;

/**
 * 规则链执行领域服务接口
 * 定义规则链执行的核心操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 18:11
 */

public interface RuleChainExecutionService {
  public BusinessContext execute(RuleChain ruleChain, BusinessContext context);
}

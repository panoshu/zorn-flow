package com.zornflow.domain.rule.service;

import com.ddd.contract.service.BaseDomainService;
import com.zornflow.domain.rule.config.RuleConfig;

import java.util.Map;

/**
 * 规则匹配器接口
 * 领域层定义规则匹配的抽象契约，不依赖具体技术实现
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 18:01
 */

public interface RuleMatcher extends BaseDomainService {
  /**
   * 匹配规则是否需要执行
   * @param ruleConfig 规则配置
   * @param context 上下文参数
   * @return 是否匹配成功
   */
  boolean matches(RuleConfig ruleConfig, Map<String, Object> context);
}

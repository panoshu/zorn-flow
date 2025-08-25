package com.zornflow.domain.rule.converter;

import com.zornflow.domain.rule.config.RuleChainConfig;
import com.zornflow.domain.rule.entity.RuleChain;

/**
 * 规则链配置转换器接口
 * 定义将规则链配置转换为规则链领域对象的契约
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 18:09
 */

public interface RuleChainConverter {
  /**
   * 将规则链配置转换为规则链领域对象
   * @param config 规则链配置
   * @return 规则链领域对象
   */
  RuleChain convert(RuleChainConfig config);
}

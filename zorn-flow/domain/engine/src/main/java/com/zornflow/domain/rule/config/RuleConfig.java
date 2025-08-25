package com.zornflow.domain.rule.config;

import com.zornflow.domain.rule.types.Priority;
import com.zornflow.domain.rule.types.RuleId;
import com.zornflow.domain.rule.types.RuleName;

import java.util.Map;

/**
 * 规则配置接口契约
 * 定义单个规则配置应包含的基本信息
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 18:05
 */

public interface RuleConfig {
  /**
   * 获取规则ID
   * @return 规则ID
   */
  RuleId getId();

  /**
   * 获取规则名称
   * @return 规则名称
   */
  RuleName getName();

  /**
   * 获取规则优先级
   * @return 规则优先级
   */
  Priority getPriority();

  /**
   * 获取规则类型
   * @return 规则类型
   */
  String getType();

  /**
   * 获取规则参数配置
   * @return 参数配置键值对
   */
  Map<String, Object> getParameters();
}

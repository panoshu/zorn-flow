package com.zornflow.domain.rule.config;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.domain.rule.types.RuleChainName;

import java.util.List;

/**
 * 规则链配置接口契约
 * 定义规则链配置应包含的基本信息
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 18:06
 */

public interface RuleChainConfig {
  /**
   * 获取规则链ID
   *
   * @return 规则链ID
   */
  RuleChainId getId();

  /**
   * 获取规则链名称
   *
   * @return 规则链名称
   */
  RuleChainName getName();

  /**
   * 获取版本号
   *
   * @return 版本号
   */
  Version getVersion();

  /**
   * 获取规则链描述
   *
   * @return 规则链描述
   */
  String getDescription();

  /**
   * 获取规则配置列表
   *
   * @return 规则配置列表
   */
  List<RuleConfig> getRules();
}

package com.zornflow.infrastructure.config.model;

import lombok.Builder;

import java.util.Map;

/**
 * 全局规则配置
 * 用于在数据库中存储可被多个规则链引用的全局规则
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Builder
public record GlobalRuleConfig(
  String id,
  String name,
  Integer priority,
  String condition,
  RuleConfig.Handler handle
) { }

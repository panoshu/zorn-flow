package com.zornflow.infrastructure.config.model;

import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 13:22
 */
public record NodeRef(
  String id,
  ProcessNode.NodeType type,
  String next,
  String ruleChain,
  Map<String, Object> properties,
  List<ConditionBranch> branches) { }

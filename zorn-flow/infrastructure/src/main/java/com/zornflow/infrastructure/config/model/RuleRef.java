package com.zornflow.infrastructure.config.model;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 13:21
 */
public record RuleRef(
  String id,
  Integer priority,
  String condition) { }

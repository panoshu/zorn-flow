package com.zornflow.infrastructure.config.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 表示数据库记录的通用状态枚举
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/7 20:22
 **/

@Getter
@RequiredArgsConstructor
public enum RecordStatus {
  ACTIVE("ACTIVE"),
  DELETED("DELETED");

  private final String dbValue;
}

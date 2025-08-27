package com.zornflow.infrastructure.config.model;

import lombok.Data;

/**
 * 规则模型
 */
@Data
public class Rule {
    private String id;
    private String name;
    private Integer priority = 100; // 默认优先级
    private String condition;
    private Handle handle;
}

package com.zornflow.infrastructure.config.model;

import lombok.Data;

/**
 * 网关条件模型
 */
@Data
public class GatewayCondition {
    private String condition;
    private String next;
}

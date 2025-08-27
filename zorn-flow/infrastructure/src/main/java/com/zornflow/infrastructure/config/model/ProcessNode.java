package com.zornflow.infrastructure.config.model;

import lombok.Data;
import java.util.Map;

/**
 * 流程节点模型
 */
@Data
public class ProcessNode {
    private String id;
    private String name;
    private String type;
    private String ruleChain;
    private Map<String, Object> properties;
    private String next;
    private List<GatewayCondition> conditions;
}

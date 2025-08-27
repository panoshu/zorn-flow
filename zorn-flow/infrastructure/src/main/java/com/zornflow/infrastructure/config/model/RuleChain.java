package com.zornflow.infrastructure.config.model;

import lombok.Data;
import java.util.List;

/**
 * 规则链模型
 */
@Data
public class RuleChain {
    private String id;
    private String name;
    private String version;
    private String description;
    private List<Rule> rules;
}

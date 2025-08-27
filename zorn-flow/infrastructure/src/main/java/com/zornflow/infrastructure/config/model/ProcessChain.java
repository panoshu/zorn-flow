package com.zornflow.infrastructure.config.model;

import lombok.Data;
import java.util.List;

/**
 * 流程链模型
 */
@Data
public class ProcessChain {
    private String id;
    private String name;
    private String version;
    private String description;
    private List<ProcessNode> nodes;
}

package com.zornflow.infrastructure.config.model;

import lombok.Data;
import java.util.Map;

/**
 * 处理器配置
 */
@Data
public class Handle {
    private String type;
    private String handler;
    private Map<String, Object> parameters;
}

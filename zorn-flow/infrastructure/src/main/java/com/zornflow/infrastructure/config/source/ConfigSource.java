package com.zornflow.infrastructure.config.source;

/**
 * 配置源标记接口
 * 定义配置来源的基本契约
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 10:00
 */
public sealed interface ConfigSource permits ReadableConfigSource, WriteableConfigSource {

    /**
     * 获取配置源名称
     * @return 配置源名称
     */
    String getSourceName();

    /**
     * 获取配置源类型
     * @return 配置源类型
     */
    ConfigSourceType getSourceType();

    /**
     * 配置源类型枚举
     */
    enum ConfigSourceType {
        YAML, DATABASE, PROPERTIES, REMOTE, COMPOSITE
    }
}

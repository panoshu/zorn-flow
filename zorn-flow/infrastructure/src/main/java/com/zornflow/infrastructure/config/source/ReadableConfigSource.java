package com.zornflow.infrastructure.config.source;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;

import java.util.Map;
import java.util.Optional;

/**
 * 可读配置源接口
 * 定义从配置源读取配置的操作
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 10:05
 */
public non-sealed interface ReadableConfigSource extends ConfigSource {

    /**
     * 加载所有规则链配置
     * @return 规则链配置映射，key为规则链ID
     */
    Map<String, RuleChainConfig> loadRuleChainConfigs();

    /**
     * 加载指定ID的规则链配置
     * @param ruleChainId 规则链ID
     * @return 规则链配置
     */
    Optional<RuleChainConfig> loadRuleChainConfig(String ruleChainId);

    /**
     * 加载所有流程链配置
     * @return 流程链配置映射，key为流程链ID
     */
    Map<String, ProcessChainConfig> loadProcessChainConfigs();

    /**
     * 加载指定ID的流程链配置
     * @param processChainId 流程链ID
     * @return 流程链配置
     */
    Optional<ProcessChainConfig> loadProcessChainConfig(String processChainId);

    /**
     * 加载全局规则配置
     * @return 全局规则配置映射，key为规则ID
     */
    Map<String, RuleConfig> loadGlobalRules();

    /**
     * 加载全局节点配置
     * @return 全局节点配置映射，key为节点ID
     */
    Map<String, ProcessNodeConfig> loadGlobalNodes();

    /**
     * 刷新配置源
     * @return 是否刷新成功
     */
    boolean refresh();
}

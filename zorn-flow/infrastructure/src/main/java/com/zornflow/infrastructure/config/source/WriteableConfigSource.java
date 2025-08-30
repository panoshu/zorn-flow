package com.zornflow.infrastructure.config.source;

import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.GlobalRuleConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;

import java.util.List;

/**
 * 可写配置源接口
 * 定义向配置源写入数据的操作，主要用于数据库等可写数据源
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
public non-sealed interface WriteableConfigSource extends ConfigSource {

    /**
     * 保存规则链配置
     */
    void saveRuleChainConfig(RuleChainConfig config);

    /**
     * 保存流程链配置
     */
    void saveProcessChainConfig(ProcessChainConfig config);

    /**
     * 保存全局规则配置
     */
    void saveGlobalRule(GlobalRuleConfig config);

    /**
     * 保存全局流程节点配置
     */
    void saveGlobalProcessNode(ProcessNodeConfig config);

    /**
     * 删除规则链配置
     */
    void deleteRuleChainConfig(String ruleChainId);

    /**
     * 删除流程链配置
     */
    void deleteProcessChainConfig(String processChainId);

    /**
     * 删除全局规则配置
     */
    void deleteGlobalRule(String ruleId);

    /**
     * 删除全局流程节点配置
     */
    void deleteGlobalProcessNode(String nodeId);

    /**
     * 批量保存规则链配置
     */
    void saveRuleChainConfigs(List<RuleChainConfig> configs);

    /**
     * 批量保存流程链配置
     */
    void saveProcessChainConfigs(List<ProcessChainConfig> configs);

    /**
     * 批量保存全局规则配置
     */
    void saveGlobalRules(List<GlobalRuleConfig> configs);

    /**
     * 批量保存全局流程节点配置
     */
    void saveGlobalProcessNodes(List<ProcessNodeConfig> configs);

    /**
     * 清空所有配置数据
     */
    void clearAll();
}

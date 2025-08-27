package com.zornflow.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zornflow.infrastructure.config.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置加载器，支持加载多个规则链和流程链配置文件
 */
@Slf4j
public class ConfigLoader {
    private final PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
    private final ObjectMapper objectMapper;

    // 缓存加载的配置
    private final Map<String, Rule> sharedRules = new ConcurrentHashMap<>();
    private final Map<String, ProcessNode> sharedNodes = new ConcurrentHashMap<>();
    private final Map<String, RuleChain> ruleChains = new ConcurrentHashMap<>();
    private final Map<String, ProcessChain> processChains = new ConcurrentHashMap<>();

    // 配置文件路径常量
    private static final String SHARED_RULES_PATH = "classpath:rules/share/*.yml";
    private static final String SHARED_NODES_PATH = "classpath:nodes/share/*.yml";
    private static final String RULE_CHAINS_PATH = "classpath:rules/*.yml";
    private static final String PROCESS_CHAINS_PATH = "classpath:processes/*.yml";

    public ConfigLoader() {
        // this.resourceLoader = new PathMatchingResourcePatternResolver();
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        // 初始化时加载所有配置
        loadAllConfigurations();
    }

    /**
     * 加载所有配置文件
     */
    public void loadAllConfigurations() {
        loadSharedRules();
        loadSharedNodes();
        loadRuleChains();
        loadProcessChains();
        log.info("所有配置加载完成 - 共享规则: {}, 共享节点: {}, 规则链: {}, 流程链: {}",
                sharedRules.size(), sharedNodes.size(), ruleChains.size(), processChains.size());
    }

    /**
     * 重新加载所有配置
     */
    public void refresh() {
        sharedRules.clear();
        sharedNodes.clear();
        ruleChains.clear();
        processChains.clear();
        loadAllConfigurations();
        log.info("所有配置已刷新");
    }

    /**
     * 加载共享规则
     */
    private void loadSharedRules() {
        loadConfigFiles(SHARED_RULES_PATH, sharedRules, Rule.class);
    }

    /**
     * 加载共享节点
     */
    private void loadSharedNodes() {
        loadConfigFiles(SHARED_NODES_PATH, sharedNodes, ProcessNode.class);
    }

    /**
     * 加载所有规则链
     */
    private void loadRuleChains() {
        Map<String, RuleChain> tempChains = new HashMap<>();
        loadConfigFiles(RULE_CHAINS_PATH, tempChains, RuleChain.class);

        // 处理规则链中的规则引用和合并
        tempChains.forEach((chainId, chain) -> {
            if (chain.getRules() != null) {
                chain.getRules().forEach(ruleRef -> {
                    // 如果是引用共享规则，合并配置
                    if (sharedRules.containsKey(ruleRef.getId())) {
                        Rule sharedRule = sharedRules.get(ruleRef.getId());
                        mergeRule(sharedRule, ruleRef);
                    }
                });
            }
            ruleChains.put(chainId, chain);
        });
    }

    /**
     * 加载所有流程链
     */
    private void loadProcessChains() {
        Map<String, ProcessChain> tempProcesses = new HashMap<>();
        loadConfigFiles(PROCESS_CHAINS_PATH, tempProcesses, ProcessChain.class);

        // 处理流程链中的节点引用和合并
        tempProcesses.forEach((processId, process) -> {
            if (process.getNodes() != null) {
                process.getNodes().forEach(nodeRef -> {
                    // 如果是引用共享节点，合并配置
                    if (sharedNodes.containsKey(nodeRef.getId())) {
                        ProcessNode sharedNode = sharedNodes.get(nodeRef.getId());
                        mergeProcessNode(sharedNode, nodeRef);
                    }
                });
            }
            processChains.put(processId, process);
        });
    }

    /**
     * 通用的配置文件加载方法
     */
    @SneakyThrows
    private <T> void loadConfigFiles(String locationPattern, Map<String, T> resultMap, Class<T> type) {
        Resource[] resources = resourceLoader.getResources(locationPattern);

        for (Resource resource : resources) {
            if (resource.exists()) {
                String filename = resource.getFilename();
                try {
                    // 解析YAML文件为Map<String, T>，键为配置ID，值为配置对象
                    Map<String, T> configMap = objectMapper.readValue(
                            resource.getInputStream(),
                            new TypeReference<Map<String, T>>() {}
                    );

                    configMap.forEach((id, config) -> {
                        resultMap.put(id, config);
                        log.debug("加载配置 - 文件: {}, ID: {}", filename, id);
                    });
                } catch (IOException e) {
                    log.error("加载配置文件失败: {}", filename, e);
                }
            }
        }
    }

    /**
     * 合并规则配置，引用配置覆盖共享配置
     */
    private void mergeRule(Rule sharedRule, Rule refRule) {
        // 如果引用规则没有指定名称，使用共享规则的名称
        if (refRule.getName() == null) {
            refRule.setName(sharedRule.getName());
        }
        // 如果引用规则没有指定优先级，使用共享规则的优先级
        if (refRule.getPriority() == null) {
            refRule.setPriority(sharedRule.getPriority());
        }
        // 如果引用规则没有指定条件，使用共享规则的条件
        if (refRule.getCondition() == null) {
            refRule.setCondition(sharedRule.getCondition());
        }
        // 如果引用规则没有指定处理器，使用共享规则的处理器
        if (refRule.getHandle() == null) {
            refRule.setHandle(sharedRule.getHandle());
        } else if (sharedRule.getHandle() != null) {
            // 合并处理器配置
            Handle refHandle = refRule.getHandle();
            Handle sharedHandle = sharedRule.getHandle();

            if (refHandle.getType() == null) {
                refHandle.setType(sharedHandle.getType());
            }
            if (refHandle.getHandler() == null) {
                refHandle.setHandler(sharedHandle.getHandler());
            }
            // 合并参数
            if (refHandle.getParameters() == null) {
                refHandle.setParameters(sharedHandle.getParameters());
            } else if (sharedHandle.getParameters() != null) {
                Map<String, Object> mergedParams = new HashMap<>(sharedHandle.getParameters());
                mergedParams.putAll(refHandle.getParameters());
                refHandle.setParameters(mergedParams);
            }
        }
    }

    /**
     * 合并流程节点配置，引用配置覆盖共享配置
     */
    private void mergeProcessNode(ProcessNode sharedNode, ProcessNode refNode) {
        if (refNode.getName() == null) {
            refNode.setName(sharedNode.getName());
        }
        if (refNode.getType() == null) {
            refNode.setType(sharedNode.getType());
        }
        if (refNode.getRuleChain() == null) {
            refNode.setRuleChain(sharedNode.getRuleChain());
        }
        // 合并属性
        if (refNode.getProperties() == null) {
            refNode.setProperties(sharedNode.getProperties());
        } else if (sharedNode.getProperties() != null) {
            Map<String, Object> mergedProps = new HashMap<>(sharedNode.getProperties());
            mergedProps.putAll(refNode.getProperties());
            refNode.setProperties(mergedProps);
        }
    }

    // 获取配置的方法
    public Rule getSharedRule(String ruleId) {
        return sharedRules.get(ruleId);
    }

    public ProcessNode getSharedNode(String nodeId) {
        return sharedNodes.get(nodeId);
    }

    public RuleChain getRuleChain(String chainId) {
        return ruleChains.get(chainId);
    }

    public ProcessChain getProcessChain(String processId) {
        return processChains.get(processId);
    }

    public Map<String, RuleChain> getAllRuleChains() {
        return new HashMap<>(ruleChains);
    }

    public Map<String, ProcessChain> getAllProcessChains() {
        return new HashMap<>(processChains);
    }
}

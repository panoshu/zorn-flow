package com.zornflow.infrastructure.converter;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 配置转换服务
 * <p>
 * 作为基础设施层配置模型与领域层实体之间的转换桥梁，负责：
 * 1. 封装MapStruct映射器的复杂性
 * 2. 提供统一的转换接口
 * 3. 处理转换过程中的异常和日志
 * 4. 支持批量转换和单个转换
 * </p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li><strong>防御式编程</strong>：处理null输入，返回Optional防止NPE</li>
 *   <li><strong>失败快速</strong>：转换异常立即抛出，便于问题定位</li>
 *   <li><strong>日志记录</strong>：记录转换过程，便于调试和监控</li>
 *   <li><strong>类型安全</strong>：利用泛型确保编译时类型检查</li>
 * </ul>
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigConverter {

    private final RuleMapper ruleMapper;
    private final ProcessMapper processMapper;

    // ============================= 规则链转换 =============================

    /**
     * 转换单个规则链配置为领域实体
     *
     * @param config 规则链配置对象，可以为null
     * @return 规则链领域实体的Optional包装
     * @throws IllegalArgumentException 当配置数据无效时
     */
    public Optional<RuleChain> convertRuleChain(RuleChainConfig config) {
        if (config == null) {
            log.debug("RuleChainConfig is null, returning empty Optional");
            return Optional.empty();
        }

        try {
            log.debug("Converting RuleChainConfig [id={}, name={}, rulesCount={}]",
                config.id(), config.name(),
                config.rules() != null ? config.rules().size() : 0);

            RuleChain ruleChain = ruleMapper.toRuleChain(config);

            log.debug("Successfully converted RuleChain [id={}, ruleCount={}]",
                ruleChain.getId().value(), ruleChain.getRuleCount());

            return Optional.of(ruleChain);

        } catch (Exception e) {
            log.error("Failed to convert RuleChainConfig [id={}, name={}]: {}",
                config.id(), config.name(), e.getMessage(), e);
            throw new ConfigConverterException(
                "转换规则链配置失败 [id=" + config.id() + "]", e);
        }
    }

    /**
     * 批量转换规则链配置列表
     *
     * @param configs 规则链配置列表，可以为null或空
     * @return 成功转换的规则链列表，失败的配置会被跳过并记录日志
     */
    public List<RuleChain> convertRuleChains(List<RuleChainConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.debug("RuleChainConfig list is null or empty, returning empty list");
            return List.of();
        }

        log.info("Starting batch conversion of {} RuleChainConfigs", configs.size());

        List<RuleChain> result = configs.stream()
            .map(this::convertRuleChainSafely)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        log.info("RuleChains conversion completed: {} successful, {} failed",
            result.size(), configs.size() - result.size());

        return result;
    }

    /**
     * 安全转换单个规则链，捕获异常并返回Optional.empty()
     */
    private Optional<RuleChain> convertRuleChainSafely(RuleChainConfig config) {
        try {
            return convertRuleChain(config);
        } catch (Exception e) {
            log.warn("Skipping invalid RuleChainConfig [id={}]: {}",
                config != null ? config.id() : "null", e.getMessage());
            return Optional.empty();
        }
    }

    // ============================= 流程链转换 =============================

    /**
     * 转换单个流程链配置为领域实体
     *
     * @param config 流程链配置对象，可以为null
     * @return 流程链领域实体的Optional包装
     * @throws IllegalArgumentException 当配置数据无效时
     */
    public Optional<ProcessChain> convertProcessChain(ProcessChainConfig config) {
        if (config == null) {
            log.debug("ProcessChainConfig is null, returning empty Optional");
            return Optional.empty();
        }

        try {
            log.debug("Converting ProcessChainConfig [id={}, name={}, nodesCount={}]",
                config.id(), config.name(),
                config.nodes() != null ? config.nodes().size() : 0);

            ProcessChain processChain = processMapper.toProcessChain(config);

            log.debug("Successfully converted ProcessChain [id={}, nodeCount={}]",
                processChain.getId().value(), processChain.getAllNodes().size());

            return Optional.of(processChain);

        } catch (Exception e) {
            log.error("Failed to convert ProcessChainConfig [id={}, name={}]: {}",
                config.id(), config.name(), e.getMessage(), e);
            throw new ConfigConverterException(
                "转换流程链配置失败 [id=" + config.id() + "]", e);
        }
    }

    /**
     * 批量转换流程链配置列表
     *
     * @param configs 流程链配置列表，可以为null或空
     * @return 成功转换的流程链列表，失败的配置会被跳过并记录日志
     */
    public List<ProcessChain> convertProcessChains(List<ProcessChainConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.debug("ProcessChainConfig list is null or empty, returning empty list");
            return List.of();
        }

        log.info("Starting batch conversion of {} ProcessChainConfigs", configs.size());

        List<ProcessChain> result = configs.stream()
            .map(this::convertProcessChainSafely)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        log.info("ProcessChains conversion completed: {} successful, {} failed",
            result.size(), configs.size() - result.size());

        return result;
    }

    /**
     * 安全转换单个流程链，捕获异常并返回Optional.empty()
     */
    private Optional<ProcessChain> convertProcessChainSafely(ProcessChainConfig config) {
        try {
            return convertProcessChain(config);
        } catch (Exception e) {
            log.warn("Skipping invalid ProcessChainConfig [id={}]: {}",
                config != null ? config.id() : "null", e.getMessage());
            return Optional.empty();
        }
    }

    // ============================= 验证和统计 =============================

    /**
     * 验证转换器是否正确初始化
     *
     * @return 如果映射器可用则返回true
     */
    public boolean isReady() {
        boolean ready = ruleMapper != null && processMapper != null;
        log.debug("ConfigConverter readiness check: {}", ready);
        return ready;
    }

    /**
     * 获取转换器统计信息（用于监控和调试）
     *
     * @return 转换器状态描述
     */
    public String getStatus() {
        return String.format("ConfigConverter[ruleMapper=%s, processMapper=%s, ready=%s]",
            ruleMapper != null ? ruleMapper.getClass().getSimpleName() : "null",
            processMapper != null ? processMapper.getClass().getSimpleName() : "null",
            isReady());
    }

    // ============================= 异常类 =============================

    /**
     * 配置转换异常
     * 封装转换过程中发生的各种异常
     */
    public static class ConfigConverterException extends RuntimeException {
        public ConfigConverterException(String message) {
            super(message);
        }

        public ConfigConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

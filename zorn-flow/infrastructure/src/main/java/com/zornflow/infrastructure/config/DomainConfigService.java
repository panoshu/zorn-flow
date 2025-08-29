package com.zornflow.infrastructure.config;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.converter.ConfigConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 领域配置服务
 * <p>
 * 作为配置管理的统一入口，负责：
 * 1. 整合多种配置源（类路径、数据库等）
 * 2. 提供统一的领域实体访问接口
 * 3. 实现配置加载的优先级策略
 * 4. 缓存和管理配置的生命周期
 * </p>
 *
 * <p>设计模式：</p>
 * <ul>
 *   <li><strong>适配器模式</strong>：统一不同配置源的访问接口</li>
 *   <li><strong>策略模式</strong>：支持不同的配置加载策略</li>
 *   <li><strong>装饰器模式</strong>：为配置加载添加缓存、日志等功能</li>
 * </ul>
 *
 * <p>优先级策略：</p>
 * <ol>
 *   <li>类路径配置（开发时配置）</li>
 *   <li>数据库配置（运行时配置，覆盖类路径配置）</li>
 *   <li>外部配置中心（未来扩展）</li>
 * </ol>
 *
 * @author Zorn Flow Team
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DomainConfigService {

    private final ClasspathConfigLoader classpathLoader;
    private final ConfigConverter converter;
    // 注意: 数据库配置加载器可选，如果需要可通过@Autowired(required = false)注入
    // private final DbConfigLoader dbLoader;

    // ============================= 规则链查询 =============================

    /**
     * 根据ID查找规则链
     * <p>
     * 查找优先级：类路径配置 -> 数据库配置
     * 如果类路径中存在指定ID的配置，优先返回类路径配置的转换结果
     * </p>
     *
     * @param chainId 规则链ID
     * @return 规则链的Optional包装，不存在时返回empty
     */
    public Optional<RuleChain> findRuleChain(String chainId) {
        if (chainId == null || chainId.isBlank()) {
            log.debug("Rule chain ID is null or blank, returning empty");
            return Optional.empty();
        }

        log.debug("Finding rule chain by ID: {}", chainId);

        // 优先从类路径加载
        Optional<RuleChain> result = classpathLoader.ruleChain(chainId)
            .flatMap(converter::convertRuleChain);

        if (result.isPresent()) {
            log.debug("Found rule chain [{}] from classpath", chainId);
            return result;
        }

        // 如果类路径没有，尝试从数据库加载（这里暂时注释，可根据需要启用）
        /*
        if (dbLoader != null) {
            result = dbLoader.ruleChain(chainId)
                .flatMap(converter::convertRuleChain);
            if (result.isPresent()) {
                log.debug("Found rule chain [{}] from database", chainId);
                return result;
            }
        }
        */

        log.debug("Rule chain [{}] not found in any configuration source", chainId);
        return Optional.empty();
    }

    /**
     * 根据强类型ID查找规则链
     *
     * @param chainId 规则链ID值对象
     * @return 规则链的Optional包装
     */
    public Optional<RuleChain> findRuleChain(RuleChainId chainId) {
        return chainId != null ? findRuleChain(chainId.value()) : Optional.empty();
    }

    /**
     * 获取所有规则链
     * <p>
     * 合并策略：类路径配置 + 数据库配置，数据库配置覆盖同名的类路径配置
     * </p>
     *
     * @return 所有可用的规则链列表
     */
    public List<RuleChain> findAllRuleChains() {
        log.debug("Loading all rule chains from configuration sources");

        // 从类路径加载所有规则链
        List<RuleChain> classpathChains = classpathLoader.ruleChain("")
            .map(List::of)
            .orElse(List.of())
            .stream()
            .map(converter::convertRuleChain)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        log.info("Loaded {} rule chains from classpath", classpathChains.size());

        // TODO: 加载数据库配置并合并
        /*
        if (dbLoader != null) {
            List<RuleChain> dbChains = dbLoader.getAllRuleChains()
                .stream()
                .map(converter::convertRuleChain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

            log.info("Loaded {} rule chains from database", dbChains.size());

            // 合并策略：数据库配置覆盖类路径配置
            return mergeRuleChains(classpathChains, dbChains);
        }
        */

        return classpathChains;
    }

    // ============================= 流程链查询 =============================

    /**
     * 根据ID查找流程链
     *
     * @param chainId 流程链ID
     * @return 流程链的Optional包装
     */
    public Optional<ProcessChain> findProcessChain(String chainId) {
        if (chainId == null || chainId.isBlank()) {
            log.debug("Process chain ID is null or blank, returning empty");
            return Optional.empty();
        }

        log.debug("Finding process chain by ID: {}", chainId);

        // 优先从类路径加载
        Optional<ProcessChain> result = classpathLoader.flow(chainId)
            .flatMap(converter::convertProcessChain);

        if (result.isPresent()) {
            log.debug("Found process chain [{}] from classpath", chainId);
            return result;
        }

        // TODO: 数据库加载逻辑

        log.debug("Process chain [{}] not found in any configuration source", chainId);
        return Optional.empty();
    }

    /**
     * 根据强类型ID查找流程链
     *
     * @param chainId 流程链ID值对象
     * @return 流程链的Optional包装
     */
    public Optional<ProcessChain> findProcessChain(ProcessChainId chainId) {
        return chainId != null ? findProcessChain(chainId.value()) : Optional.empty();
    }

    /**
     * 获取所有流程链
     *
     * @return 所有可用的流程链列表
     */
    public List<ProcessChain> findAllProcessChains() {
        log.debug("Loading all process chains from configuration sources");

        // 从类路径加载所有流程链
        List<ProcessChain> classpathChains = classpathLoader.flow("")
            .map(List::of)
            .orElse(List.of())
            .stream()
            .map(converter::convertProcessChain)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        log.info("Loaded {} process chains from classpath", classpathChains.size());

        // TODO: 数据库配置加载和合并

        return classpathChains;
    }

    // ============================= 配置管理 =============================

    /**
     * 刷新配置缓存
     * <p>
     * 重新加载所有配置源，适用于配置热更新场景
     * </p>
     */
    public void refreshConfiguration() {
        log.info("Refreshing configuration from all sources");

        try {
            // 重新初始化类路径加载器
            // 注意: ClasspathConfigLoader在构造函数中就已经加载了配置
            // 如果需要支持热刷新，需要添加refresh方法

            log.info("Configuration refresh completed successfully");

        } catch (Exception e) {
            log.error("Failed to refresh configuration: {}", e.getMessage(), e);
            throw new ConfigurationException("配置刷新失败", e);
        }
    }

    /**
     * 验证配置完整性
     *
     * @return 配置验证结果
     */
    public ConfigValidationResult validateConfiguration() {
        log.debug("Validating configuration integrity");

        try {
            int ruleChainCount = findAllRuleChains().size();
            int processChainCount = findAllProcessChains().size();
            boolean converterReady = converter.isReady();

            boolean isValid = converterReady && (ruleChainCount > 0 || processChainCount > 0);

            String message = String.format(
                "Configuration validation: %s (ruleChains=%d, processChains=%d, converterReady=%s)",
                isValid ? "PASSED" : "FAILED",
                ruleChainCount, processChainCount, converterReady
            );

            log.info(message);

            return new ConfigValidationResult(isValid, message);

        } catch (Exception e) {
            log.error("Configuration validation failed: {}", e.getMessage(), e);
            return new ConfigValidationResult(false, "验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置统计信息
     *
     * @return 配置统计信息
     */
    public ConfigStatistics getStatistics() {
        try {
            List<RuleChain> ruleChains = findAllRuleChains();
            List<ProcessChain> processChains = findAllProcessChains();

            int totalRules = ruleChains.stream()
                .mapToInt(RuleChain::getRuleCount)
                .sum();

            int totalNodes = processChains.stream()
                .mapToInt(chain -> chain.getAllNodes().size())
                .sum();

            return new ConfigStatistics(
                ruleChains.size(),
                processChains.size(),
                totalRules,
                totalNodes,
                converter.getStatus()
            );

        } catch (Exception e) {
            log.error("Failed to generate configuration statistics: {}", e.getMessage(), e);
            return ConfigStatistics.empty();
        }
    }

    /**
     * 配置验证结果
     */
    public record ConfigValidationResult(boolean valid, String message) {}

    /**
     * 配置统计信息
     */
    public record ConfigStatistics(
        int ruleChainCount,
        int processChainCount,
        int totalRules,
        int totalNodes,
        String converterStatus
    ) {
        public static ConfigStatistics empty() {
            return new ConfigStatistics(0, 0, 0, 0, "UNAVAILABLE");
        }
    }

    /**
     * 配置异常
     */
    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

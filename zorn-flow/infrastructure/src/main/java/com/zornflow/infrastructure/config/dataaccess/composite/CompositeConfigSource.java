package com.zornflow.infrastructure.config.dataaccess.composite;

import com.zornflow.infrastructure.config.model.*;
import com.zornflow.infrastructure.config.source.ConfigSource;
import com.zornflow.infrastructure.config.source.ReadableConfigSource;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.source.WriteableConfigSource;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 组合配置源实现（无锁优化版本）
 * 支持多数据源优先级管理、无锁缓存和原子性刷新
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
public class CompositeConfigSource implements ReadWriteConfigSource {

    private final List<ReadableConfigSource> readableSources;
    private final List<WriteableConfigSource> writeableSources;
    private final ConfigSourceCache cache;

    // 使用原子变量替代锁，提高性能
    private final AtomicBoolean cacheEnabled = new AtomicBoolean(true);
    private final AtomicLong cacheExpireTime = new AtomicLong(300000L); // 5分钟过期


    /**
     * 支持混合配置源的构造函数（推荐使用）
     * 自动分类只读、只写、读写配置源
     */
    public CompositeConfigSource(List<? extends ConfigSource> allSources) {
        this.readableSources = new ArrayList<>();
        this.writeableSources = new ArrayList<>();
        this.cache = new ConfigSourceCache();

        // 自动分类配置源
        for (ConfigSource source : allSources) {
            if (source == null) continue;

            // 添加到可读源列表
            if (source instanceof ReadableConfigSource) {
                readableSources.add((ReadableConfigSource) source);
            }

            // 添加到可写源列表
            if (source instanceof WriteableConfigSource) {
                writeableSources.add((WriteableConfigSource) source);
            }
        }

        // 按优先级排序可写源
        writeableSources.sort((a, b) -> {
            int priorityA = (a instanceof ReadWriteConfigSource) ? ((ReadWriteConfigSource) a).getPriority() : Integer.MAX_VALUE;
            int priorityB = (b instanceof ReadWriteConfigSource) ? ((ReadWriteConfigSource) b).getPriority() : Integer.MAX_VALUE;
            return Integer.compare(priorityA, priorityB);
        });

        log.info("初始化混合组合配置源 - 可读源: {}, 可写源: {}",
            readableSources.size(), writeableSources.size());

        // 输出配置源详情
        readableSources.forEach(source ->
            log.info("可读配置源: {} - 类型: {}", source.getSourceName(), source.getSourceType()));
        writeableSources.forEach(source ->
            log.info("可写配置源: {} - 类型: {}", source.getSourceName(), source.getSourceType()));
    }

    @Override
    public String getSourceName() {
        return "CompositeConfigSource";
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.COMPOSITE;
    }

    @Override
    public int getPriority() {
        return 0; // 组合配置源具有最高优先级
    }

    @Override
    public boolean isAvailable() {
        return !readableSources.isEmpty();
    }

    // =============== 读取操作（支持无锁缓存和优先级） ===============

    @Override
    public Map<String, RuleChainConfig> loadRuleChainConfigs() {
        String cacheKey = "all_rule_chains";
        return loadWithCache(cacheKey, () -> {
            Map<String, RuleChainConfig> result = new HashMap<>();

            // 按优先级从高到低合并配置，低优先级的配置会被高优先级覆盖
            for (int i = readableSources.size() - 1; i >= 0; i--) {
                ReadableConfigSource source = readableSources.get(i);
                try {
                    Map<String, RuleChainConfig> sourceConfigs = source.loadRuleChainConfigs();
                    result.putAll(sourceConfigs);
                    log.debug("从 {} 加载了 {} 个规则链配置", source.getSourceName(), sourceConfigs.size());
                } catch (Exception e) {
                    log.warn("从配置源 {} 加载规则链配置失败", source.getSourceName(), e);
                }
            }

            return result;
        });
    }

    @Override
    public Optional<RuleChainConfig> loadRuleChainConfig(String ruleChainId) {
        String cacheKey = "rule_chain_" + ruleChainId;
        return loadWithCache(cacheKey, () -> {
            // 按优先级顺序查找，找到第一个就返回
            for (ReadableConfigSource source : readableSources) {
                try {
                    Optional<RuleChainConfig> config = source.loadRuleChainConfig(ruleChainId);
                    if (config.isPresent()) {
                        log.debug("从 {} 加载规则链配置: {}", source.getSourceName(), ruleChainId);
                        return config;
                    }
                } catch (Exception e) {
                    log.warn("从配置源 {} 加载规则链配置失败: {}", source.getSourceName(), ruleChainId, e);
                }
            }
            return Optional.empty();
        });
    }

    @Override
    public Map<String, ProcessChainConfig> loadProcessChainConfigs() {
        String cacheKey = "all_process_chains";
        return loadWithCache(cacheKey, () -> {
            Map<String, ProcessChainConfig> result = new HashMap<>();

            // 按优先级从高到低合并配置
            for (int i = readableSources.size() - 1; i >= 0; i--) {
                ReadableConfigSource source = readableSources.get(i);
                try {
                    Map<String, ProcessChainConfig> sourceConfigs = source.loadProcessChainConfigs();
                    result.putAll(sourceConfigs);
                    log.debug("从 {} 加载了 {} 个流程链配置", source.getSourceName(), sourceConfigs.size());
                } catch (Exception e) {
                    log.warn("从配置源 {} 加载流程链配置失败", source.getSourceName(), e);
                }
            }

            return result;
        });
    }

    @Override
    public Optional<ProcessChainConfig> loadProcessChainConfig(String processChainId) {
        String cacheKey = "process_chain_" + processChainId;
        return loadWithCache(cacheKey, () -> {
            // 按优先级顺序查找
            for (ReadableConfigSource source : readableSources) {
                try {
                    Optional<ProcessChainConfig> config = source.loadProcessChainConfig(processChainId);
                    if (config.isPresent()) {
                        log.debug("从 {} 加载流程链配置: {}", source.getSourceName(), processChainId);
                        return config;
                    }
                } catch (Exception e) {
                    log.warn("从配置源 {} 加载流程链配置失败: {}", source.getSourceName(), processChainId, e);
                }
            }
            return Optional.empty();
        });
    }

    @Override
    public Map<String, RuleConfig> loadGlobalRules() {
        String cacheKey = "all_global_rules";
        return loadWithCache(cacheKey, () -> {
            Map<String, RuleConfig> result = new HashMap<>();

            // 按优先级从高到低合并配置
            for (int i = readableSources.size() - 1; i >= 0; i--) {
                ReadableConfigSource source = readableSources.get(i);
                try {
                    Map<String, RuleConfig> sourceConfigs = source.loadGlobalRules();
                    result.putAll(sourceConfigs);
                    log.debug("从 {} 加载了 {} 个全局规则", source.getSourceName(), sourceConfigs.size());
                } catch (Exception e) {
                    log.warn("从配置源 {} 加载全局规则失败", source.getSourceName(), e);
                }
            }

            return result;
        });
    }

    @Override
    public Map<String, ProcessNodeConfig> loadGlobalNodes() {
        String cacheKey = "all_global_nodes";
        return loadWithCache(cacheKey, () -> {
            Map<String, ProcessNodeConfig> result = new HashMap<>();

            // 按优先级从高到低合并配置
            for (int i = readableSources.size() - 1; i >= 0; i--) {
                ReadableConfigSource source = readableSources.get(i);
                try {
                    Map<String, ProcessNodeConfig> sourceConfigs = source.loadGlobalNodes();
                    result.putAll(sourceConfigs);
                    log.debug("从 {} 加载了 {} 个全局节点", source.getSourceName(), sourceConfigs.size());
                } catch (Exception e) {
                    log.warn("从配置源 {} 加载全局节点失败", source.getSourceName(), e);
                }
            }

            return result;
        });
    }

    // =============== 写入操作（智能路由到目标数据源） ===============

    @Override
    public void saveRuleChainConfig(RuleChainConfig config) {
        WriteableConfigSource targetSource = findTargetWriteableSource(config.id(), "rule_chain");
        if (targetSource != null) {
            targetSource.saveRuleChainConfig(config);
            cache.remove("rule_chain_" + config.id());
            cache.remove("all_rule_chains");
            log.info("保存规则链配置到 {}: {}", targetSource.getSourceName(), config.id());
        } else {
            throw new IllegalStateException("没有找到可写的配置源来保存规则链: " + config.id());
        }
    }

    @Override
    public void saveProcessChainConfig(ProcessChainConfig config) {
        WriteableConfigSource targetSource = findTargetWriteableSource(config.id(), "process_chain");
        if (targetSource != null) {
            targetSource.saveProcessChainConfig(config);
            cache.remove("process_chain_" + config.id());
            cache.remove("all_process_chains");
            log.info("保存流程链配置到 {}: {}", targetSource.getSourceName(), config.id());
        } else {
            throw new IllegalStateException("没有找到可写的配置源来保存流程链: " + config.id());
        }
    }

    @Override
    public void saveGlobalRule(GlobalRuleConfig config) {
        WriteableConfigSource targetSource = findTargetWriteableSource(config.id(), "global_rule");
        if (targetSource != null) {
            targetSource.saveGlobalRule(config);
            cache.remove("all_global_rules");
            log.info("保存全局规则到 {}: {}", targetSource.getSourceName(), config.id());
        } else {
            throw new IllegalStateException("没有找到可写的配置源来保存全局规则: " + config.id());
        }
    }

    @Override
    public void saveGlobalProcessNode(ProcessNodeConfig config) {
        WriteableConfigSource targetSource = findTargetWriteableSource(config.id(), "global_node");
        if (targetSource != null) {
            targetSource.saveGlobalProcessNode(config);
            cache.remove("all_global_nodes");
            log.info("保存全局节点到 {}: {}", targetSource.getSourceName(), config.id());
        } else {
            throw new IllegalStateException("没有找到可写的配置源来保存全局节点: " + config.id());
        }
    }

    @Override
    public void deleteRuleChainConfig(String ruleChainId) {
        WriteableConfigSource targetSource = findTargetWriteableSource(ruleChainId, "rule_chain");
        if (targetSource != null) {
            targetSource.deleteRuleChainConfig(ruleChainId);
            cache.remove("rule_chain_" + ruleChainId);
            cache.remove("all_rule_chains");
            log.info("从 {} 删除规则链配置: {}", targetSource.getSourceName(), ruleChainId);
        }
    }

    @Override
    public void deleteProcessChainConfig(String processChainId) {
        WriteableConfigSource targetSource = findTargetWriteableSource(processChainId, "process_chain");
        if (targetSource != null) {
            targetSource.deleteProcessChainConfig(processChainId);
            cache.remove("process_chain_" + processChainId);
            cache.remove("all_process_chains");
            log.info("从 {} 删除流程链配置: {}", targetSource.getSourceName(), processChainId);
        }
    }

    @Override
    public void deleteGlobalRule(String ruleId) {
        WriteableConfigSource targetSource = findTargetWriteableSource(ruleId, "global_rule");
        if (targetSource != null) {
            targetSource.deleteGlobalRule(ruleId);
            cache.remove("all_global_rules");
            log.info("从 {} 删除全局规则: {}", targetSource.getSourceName(), ruleId);
        }
    }

    @Override
    public void deleteGlobalProcessNode(String nodeId) {
        WriteableConfigSource targetSource = findTargetWriteableSource(nodeId, "global_node");
        if (targetSource != null) {
            targetSource.deleteGlobalProcessNode(nodeId);
            cache.remove("all_global_nodes");
            log.info("从 {} 删除全局节点: {}", targetSource.getSourceName(), nodeId);
        }
    }

    @Override
    public void saveRuleChainConfigs(List<RuleChainConfig> configs) {
        for (RuleChainConfig config : configs) {
            saveRuleChainConfig(config);
        }
    }

    @Override
    public void saveProcessChainConfigs(List<ProcessChainConfig> configs) {
        for (ProcessChainConfig config : configs) {
            saveProcessChainConfig(config);
        }
    }

    @Override
    public void saveGlobalRules(List<GlobalRuleConfig> configs) {
        for (GlobalRuleConfig config : configs) {
            saveGlobalRule(config);
        }
    }

    @Override
    public void saveGlobalProcessNodes(List<ProcessNodeConfig> configs) {
        for (ProcessNodeConfig config : configs) {
            saveGlobalProcessNode(config);
        }
    }

    @Override
    public void clearAll() {
        for (WriteableConfigSource source : writeableSources) {
            try {
                source.clearAll();
                log.info("清空配置源: {}", source.getSourceName());
            } catch (Exception e) {
                log.error("清空配置源失败: {}", source.getSourceName(), e);
            }
        }
        clearCache();
    }

    // =============== 缓存和刷新管理 ===============

    @Override
    public boolean refresh() {
        try {
            cache.clear();
            boolean allSuccess = true;

            for (ReadableConfigSource source : readableSources) {
                try {
                    boolean success = source.refresh();
                    if (!success) {
                        allSuccess = false;
                        log.warn("刷新配置源失败: {}", source.getSourceName());
                    }
                } catch (Exception e) {
                    allSuccess = false;
                    log.error("刷新配置源异常: {}", source.getSourceName(), e);
                }
            }

            log.info("组合配置源刷新完成，成功状态: {}", allSuccess);
            return allSuccess;
        } catch (Exception e) {
            log.error("组合配置源刷新失败", e);
            return false;
        }
    }

    @Override
    public void clearCache() {
        cache.clear();
        log.debug("清空组合配置源缓存");
    }

    // =============== 配置管理方法 ===============

    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled.set(enabled);
        if (!enabled) {
            clearCache();
        }
        log.info("设置缓存状态: {}", enabled);
    }

    public void setCacheExpireTime(long expireTimeMs) {
        this.cacheExpireTime.set(expireTimeMs);
        log.info("设置缓存过期时间: {} ms", expireTimeMs);
    }

    // =============== 私有辅助方法 ===============

    /**
     * 查找目标配置对象所在的可写数据源
     */
    private WriteableConfigSource findTargetWriteableSource(String configId, String configType) {
        // 1. 首先在可读源中查找该配置对象
        for (ReadableConfigSource readableSource : readableSources) {
            if (configExists(readableSource, configId, configType)) {
                // 2. 查找对应的可写源
                for (WriteableConfigSource writeableSource : writeableSources) {
                    if (writeableSource.getSourceName().equals(readableSource.getSourceName())) {
                        return writeableSource;
                    }
                }
            }
        }

        // 3. 如果没有找到，返回第一个可写源（用于新配置）
        return writeableSources.isEmpty() ? null : writeableSources.getFirst();
    }

    /**
     * 检查配置对象是否在指定数据源中存在
     */
    private boolean configExists(ReadableConfigSource source, String configId, String configType) {
        try {
          return switch (configType) {
            case "rule_chain" -> source.loadRuleChainConfig(configId).isPresent();
            case "process_chain" -> source.loadProcessChainConfig(configId).isPresent();
            case "global_rule" -> source.loadGlobalRules().containsKey(configId);
            case "global_node" -> source.loadGlobalNodes().containsKey(configId);
            default -> false;
          };
        } catch (Exception e) {
            log.debug("检查配置存在性时出错: {} in {}", configId, source.getSourceName(), e);
            return false;
        }
    }

    /**
     * 带缓存的加载方法（无锁实现）
     */
    @SuppressWarnings("unchecked")
    private <T> T loadWithCache(String cacheKey, java.util.function.Supplier<T> loader) {
        if (!cacheEnabled.get()) {
            return loader.get();
        }

        // 先尝试从缓存获取
        T cached = (T) cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，加载数据并缓存
        T result = loader.get();
        cache.put(cacheKey, result, cacheExpireTime.get());
        return result;
    }

    /**
     * 无锁缓存实现（使用ConcurrentHashMap保证线程安全）
     */
    private static class ConfigSourceCache {
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

        public void put(String key, Object value, long expireTimeMs) {
            long expireAt = System.currentTimeMillis() + expireTimeMs;
            cache.put(key, new CacheEntry(value, expireAt));
        }

        public Object get(String key) {
            CacheEntry entry = cache.get(key);
            if (entry == null || entry.isExpired()) {
                cache.remove(key);
                return null;
            }
            return entry.value;
        }

        public void remove(String key) {
            cache.remove(key);
        }

        public void clear() {
            cache.clear();
        }

      private record CacheEntry(Object value, long expireAt) {

        boolean isExpired() {
          return System.currentTimeMillis() > expireAt;
        }
      }
    }
}

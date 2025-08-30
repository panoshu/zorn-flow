package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.mapper.RuleConfigMapper;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 组合规则链Repository实现
 * 支持从多数据源读写规则链配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "zornflow.config.repository.type", havingValue = "composite", matchIfMissing = true)
public class CompositeRuleChainRepository implements RuleChainRepository {

    private final ReadWriteConfigSource readWriteConfigSource;

    @Override
    public Optional<RuleChain> findById(RuleChainId id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            Optional<RuleChainConfig> configOpt = readWriteConfigSource.loadRuleChainConfig(id.value());
            if (configOpt.isPresent()) {
                RuleChain ruleChain = RuleConfigMapper.INSTANCE.toRuleChain(configOpt.get());
                log.debug("从组合配置源加载规则链: {}", id.value());
                return Optional.of(ruleChain);
            }

            log.debug("未找到规则链配置: {}", id.value());
            return Optional.empty();
        } catch (Exception e) {
            log.error("加载规则链失败: {}", id.value(), e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<RuleChain> findAll() {
        try {
            Map<String, RuleChainConfig> allConfigs = readWriteConfigSource.loadRuleChainConfigs();
            Collection<RuleChain> ruleChains = allConfigs.values()
                .stream()
                .map(RuleConfigMapper.INSTANCE::toRuleChain)
                .toList();

            log.debug("从组合配置源加载所有规则链，共 {} 个", ruleChains.size());
            return ruleChains;
        } catch (Exception e) {
            log.error("加载所有规则链失败", e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public RuleChain save(RuleChain aggregateRoot) {
        try {
            // 将领域对象转换为配置对象
            RuleChainConfig config = RuleConfigMapper.INSTANCE.toRuleChainConfig(aggregateRoot);

            // 保存到配置源
            readWriteConfigSource.saveRuleChainConfig(config);

            log.info("保存规则链成功: {}", aggregateRoot.getId().value());
            return aggregateRoot;
        } catch (Exception e) {
            log.error("保存规则链失败: {}", aggregateRoot.getId().value(), e);
            throw new RuntimeException("保存规则链失败", e);
        }
    }

    @Override
    public void delete(RuleChain aggregateRoot) {
        deleteById(aggregateRoot.getId());
    }

    @Override
    public void deleteById(RuleChainId id) {
        try {
            readWriteConfigSource.deleteRuleChainConfig(id.value());
            log.info("删除规则链成功: {}", id.value());
        } catch (Exception e) {
            log.error("删除规则链失败: {}", id.value(), e);
            throw new RuntimeException("删除规则链失败", e);
        }
    }

    /**
     * 刷新配置源
     * @return 是否刷新成功
     */
    public boolean refresh() {
        try {
            boolean result = readWriteConfigSource.refresh();
            if (result) {
                log.info("规则链组合配置源刷新成功");
            } else {
                log.warn("规则链组合配置源刷新失败");
            }
            return result;
        } catch (Exception e) {
            log.error("规则链组合配置源刷新异常", e);
            return false;
        }
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        try {
            readWriteConfigSource.clearCache();
            log.info("清空规则链配置缓存成功");
        } catch (Exception e) {
            log.error("清空规则链配置缓存失败", e);
        }
    }

    /**
     * 批量保存规则链
     */
    public void saveAll(Collection<RuleChain> ruleChains) {
        for (RuleChain ruleChain : ruleChains) {
            save(ruleChain);
        }
    }

    /**
     * 检查配置源是否可用
     */
    public boolean isAvailable() {
        return readWriteConfigSource.isAvailable();
    }
}

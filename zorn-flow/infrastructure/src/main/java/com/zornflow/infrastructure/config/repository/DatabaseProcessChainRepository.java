package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.infrastructure.config.mapper.ProcessConfigMapper;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 数据库流程链Repository实现
 * 专门从数据库读写流程链配置
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "zornflow.config.repository.type", havingValue = "database")
public class DatabaseProcessChainRepository implements ProcessChainRepository {

    private final ReadWriteConfigSource readWriteConfigSource;

    @Override
    public Optional<ProcessChain> findById(ProcessChainId id) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            Optional<ProcessChainConfig> configOpt = readWriteConfigSource.loadProcessChainConfig(id.value());
            if (configOpt.isPresent()) {
                ProcessChain processChain = ProcessConfigMapper.INSTANCE.toProcessChain(configOpt.get());
                log.debug("从数据库加载流程链: {}", id.value());
                return Optional.of(processChain);
            }

            log.debug("数据库中未找到流程链配置: {}", id.value());
            return Optional.empty();
        } catch (Exception e) {
            log.error("从数据库加载流程链失败: {}", id.value(), e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<ProcessChain> findAll() {
        try {
            Map<String, ProcessChainConfig> allConfigs = readWriteConfigSource.loadProcessChainConfigs();
            Collection<ProcessChain> processChains = allConfigs.values()
                .stream()
                .map(ProcessConfigMapper.INSTANCE::toProcessChain)
                .toList();

            log.debug("从数据库加载所有流程链，共 {} 个", processChains.size());
            return processChains;
        } catch (Exception e) {
            log.error("从数据库加载所有流程链失败", e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public ProcessChain save(ProcessChain aggregateRoot) {
        try {
            // 将领域对象转换为配置对象
            ProcessChainConfig config = ProcessConfigMapper.INSTANCE.toProcessChainConfig(aggregateRoot);

            // 保存到数据库
            readWriteConfigSource.saveProcessChainConfig(config);

            log.info("保存流程链到数据库成功: {}", aggregateRoot.getId().value());
            return aggregateRoot;
        } catch (Exception e) {
            log.error("保存流程链到数据库失败: {}", aggregateRoot.getId().value(), e);
            throw new RuntimeException("保存流程链到数据库失败", e);
        }
    }

    @Override
    public void delete(ProcessChain aggregateRoot) {
        deleteById(aggregateRoot.getId());
    }

    @Override
    public void deleteById(ProcessChainId id) {
        try {
            readWriteConfigSource.deleteProcessChainConfig(id.value());
            log.info("从数据库删除流程链成功: {}", id.value());
        } catch (Exception e) {
            log.error("从数据库删除流程链失败: {}", id.value(), e);
            throw new RuntimeException("从数据库删除流程链失败", e);
        }
    }

    /**
     * 刷新数据库连接
     * @return 是否刷新成功
     */
    public boolean refresh() {
        try {
            boolean result = readWriteConfigSource.refresh();
            if (result) {
                log.info("数据库配置源刷新成功");
            } else {
                log.warn("数据库配置源刷新失败");
            }
            return result;
        } catch (Exception e) {
            log.error("数据库配置源刷新异常", e);
            return false;
        }
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        try {
            readWriteConfigSource.clearCache();
            log.info("清空数据库配置缓存成功");
        } catch (Exception e) {
            log.error("清空数据库配置缓存失败", e);
        }
    }

    /**
     * 批量保存流程链
     */
    public void saveAll(Collection<ProcessChain> processChains) {
        for (ProcessChain processChain : processChains) {
            save(processChain);
        }
    }

    /**
     * 检查数据库是否可用
     */
    public boolean isAvailable() {
        return readWriteConfigSource.isAvailable();
    }
}

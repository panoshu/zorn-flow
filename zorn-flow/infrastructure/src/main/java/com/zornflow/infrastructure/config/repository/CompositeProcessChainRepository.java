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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 组合流程链Repository实现
 * 支持从多数据源读写流程链配置
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
public class CompositeProcessChainRepository implements ProcessChainRepository {

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
        log.debug("从组合配置源加载流程链: {}", id.value());
        return Optional.of(processChain);
      }

      log.debug("未找到流程链配置: {}", id.value());
      return Optional.empty();
    } catch (Exception e) {
      log.error("加载流程链失败: {}", id.value(), e);
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

      log.debug("从组合配置源加载所有流程链，共 {} 个", processChains.size());
      return processChains;
    } catch (Exception e) {
      log.error("加载所有流程链失败", e);
      return java.util.Collections.emptyList();
    }
  }

  @Override
  public ProcessChain save(ProcessChain aggregateRoot) {
    try {
      // 将领域对象转换为配置对象
      ProcessChainConfig config = ProcessConfigMapper.INSTANCE.toProcessChainConfig(aggregateRoot);

      // 保存到配置源
      readWriteConfigSource.saveProcessChainConfig(config);

      log.info("保存流程链成功: {}", aggregateRoot.getId().value());
      return aggregateRoot;
    } catch (Exception e) {
      log.error("保存流程链失败: {}", aggregateRoot.getId().value(), e);
      throw new RuntimeException("保存流程链失败", e);
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
      log.info("删除流程链成功: {}", id.value());
    } catch (Exception e) {
      log.error("删除流程链失败: {}", id.value(), e);
      throw new RuntimeException("删除流程链失败", e);
    }
  }

  /**
   * 刷新配置源
   *
   * @return 是否刷新成功
   */
  public boolean refresh() {
    try {
      boolean result = readWriteConfigSource.refresh();
      if (result) {
        log.info("流程链组合配置源刷新成功");
      } else {
        log.warn("流程链组合配置源刷新失败");
      }
      return result;
    } catch (Exception e) {
      log.error("流程链组合配置源刷新异常", e);
      return false;
    }
  }

  /**
   * 清空缓存
   */
  public void clearCache() {
    try {
      readWriteConfigSource.clearCache();
      log.info("清空流程链配置缓存成功");
    } catch (Exception e) {
      log.error("清空流程链配置缓存失败", e);
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
   * 检查配置源是否可用
   */
  public boolean isAvailable() {
    return readWriteConfigSource.isAvailable();
  }
}

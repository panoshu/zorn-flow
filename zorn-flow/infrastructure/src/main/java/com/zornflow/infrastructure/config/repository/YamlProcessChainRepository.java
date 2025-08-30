package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.infrastructure.config.mapper.ProcessConfigMapper;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.source.ReadableConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 基于YAML配置的流程链仓库实现
 * 从YAML文件中加载流程链数据并转换为领域实体
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 12:05
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "zornflow.config.repository.type", havingValue = "yaml", matchIfMissing = true)
public class YamlProcessChainRepository implements ProcessChainRepository {

  private final ReadableConfigSource readableConfigSource;

  @Override
  public Optional<ProcessChain> findById(ProcessChainId id) {
    if (id == null) {
      return Optional.empty();
    }

    try {
      Optional<ProcessChainConfig> configOpt = readableConfigSource.loadProcessChainConfig(id.value());
      if (configOpt.isPresent()) {
        ProcessChain processChain = ProcessConfigMapper.INSTANCE.toProcessChain(configOpt.get());
        log.debug("从配置源加载流程链: {}", id.value());
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
      Map<String, ProcessChainConfig> allConfigs = readableConfigSource.loadProcessChainConfigs();
      Collection<ProcessChain> processChains = allConfigs.values()
        .stream()
        .map(ProcessConfigMapper.INSTANCE::toProcessChain)
        .toList();

      log.debug("加载所有流程链，共 {} 个", processChains.size());
      return processChains;
    } catch (Exception e) {
      log.error("加载所有流程链失败", e);
      return java.util.Collections.emptyList();
    }
  }

  @Override
  public ProcessChain save(ProcessChain aggregateRoot) {
    log.warn("YAML配置源不支持保存操作，流程链ID: {}", aggregateRoot.getId().value());
    throw new UnsupportedOperationException("YAML配置源不支持保存操作");
  }

  @Override
  public void delete(ProcessChain aggregateRoot) {
    log.warn("YAML配置源不支持删除操作，流程链ID: {}", aggregateRoot.getId().value());
    throw new UnsupportedOperationException("YAML配置源不支持删除操作");
  }

  @Override
  public void deleteById(ProcessChainId id) {
    log.warn("YAML配置源不支持删除操作，流程链ID: {}", id.value());
    throw new UnsupportedOperationException("YAML配置源不支持删除操作");
  }

  /**
   * 刷新配置源
   *
   * @return 是否刷新成功
   */
  public boolean refresh() {
    try {
      boolean result = readableConfigSource.refresh();
      if (result) {
        log.info("流程链配置源刷新成功");
      } else {
        log.warn("流程链配置源刷新失败");
      }
      return result;
    } catch (Exception e) {
      log.error("流程链配置源刷新异常", e);
      return false;
    }
  }
}

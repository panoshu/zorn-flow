package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.mapper.RuleConfigMapper;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.ReadableConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 基于YAML配置的规则链仓库实现
 * 从YAML文件中加载规则链数据并转换为领域实体
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 12:00
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "zornflow.config.repository.type", havingValue = "yaml", matchIfMissing = true)
public class YamlRuleChainRepository implements RuleChainRepository {

  private final ReadableConfigSource readableConfigSource;

  @Override
  public Optional<RuleChain> findById(RuleChainId id) {
    if (id == null) {
      return Optional.empty();
    }

    try {
      Optional<RuleChainConfig> configOpt = readableConfigSource.loadRuleChainConfig(id.value());
      if (configOpt.isPresent()) {
        RuleChain ruleChain = RuleConfigMapper.INSTANCE.toRuleChain(configOpt.get());
        log.debug("从配置源加载规则链: {}", id.value());
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
      Map<String, RuleChainConfig> allConfigs = readableConfigSource.loadRuleChainConfigs();
      Collection<RuleChain> ruleChains = allConfigs.values()
        .stream()
        .map(RuleConfigMapper.INSTANCE::toRuleChain)
        .toList();

      log.debug("加载所有规则链，共 {} 个", ruleChains.size());
      return ruleChains;
    } catch (Exception e) {
      log.error("加载所有规则链失败", e);
      return java.util.Collections.emptyList();
    }
  }

  @Override
  public RuleChain save(RuleChain aggregateRoot) {
    log.warn("YAML配置源不支持保存操作，规则链ID: {}", aggregateRoot.getId().value());
    throw new UnsupportedOperationException("YAML配置源不支持保存操作");
  }

  @Override
  public void delete(RuleChain aggregateRoot) {
    log.warn("YAML配置源不支持删除操作，规则链ID: {}", aggregateRoot.getId().value());
    throw new UnsupportedOperationException("YAML配置源不支持删除操作");
  }

  @Override
  public void deleteById(RuleChainId id) {
    log.warn("YAML配置源不支持删除操作，规则链ID: {}", id.value());
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
        log.info("规则链配置源刷新成功");
      } else {
        log.warn("规则链配置源刷新失败");
      }
      return result;
    } catch (Exception e) {
      log.error("规则链配置源刷新异常", e);
      return false;
    }
  }
}

package com.zornflow.infrastructure.repository;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/1 17:30
 **/

import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.cache.CachingRuleChainCompositeConfigSourceDecorator;
import com.zornflow.infrastructure.repository.mapper.RuleConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RuleChainRepository的实现。
 * 适配器模式：将对ConfigSource的调用适配成领域层Repository接口。
 * 防腐层：在DTO和领域实体之间进行转换。
 */
@Repository
@RequiredArgsConstructor
public class RuleChainRepositoryImpl implements RuleChainRepository {

  private final CachingRuleChainCompositeConfigSourceDecorator configSource;
  private final RuleConfigMapper mapper;

  @Override
  @SneakyThrows
  public Optional<RuleChain> findById(RuleChainId ruleChainId) {
    return configSource.load(ruleChainId.value())
      .map(mapper::toDomain);
  }

  @Override
  @SneakyThrows
  public List<RuleChain> findAll() {
    return configSource.loadAll().values().stream()
      .map(mapper::toDomain)
      .collect(Collectors.toList());
  }

  @Override
  @SneakyThrows
  public RuleChain save(RuleChain ruleChain) {
    // 1. 将领域实体转换为DTO
    RuleChainConfig dtoToSave = mapper.toDto(ruleChain);
    // 2. 调用数据源的save方法，它现在返回一个包含已保存状态的DTO的Optional
    Optional<RuleChainConfig> savedDtoOptional = configSource.save(dtoToSave);
    // 3. 将返回的DTO转换回领域实体并返回
    // 如果数据源未能返回已保存的实体，则抛出异常，因为Repository契约要求返回一个实例
    return savedDtoOptional
      .map(mapper::toDomain)
      .orElseThrow(() -> new IllegalStateException("Config source failed to return the saved entity for ID: " + ruleChain.getId().value()));
  }

  @Override
  @SneakyThrows
  public void delete(RuleChain ruleChain) {
    this.deleteById(ruleChain.getId());
  }

  @Override
  public void deleteById(RuleChainId ruleChainId) {
    configSource.delete(ruleChainId.value());
  }
}

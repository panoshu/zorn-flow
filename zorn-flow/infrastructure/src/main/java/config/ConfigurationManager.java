package config;

import com.zornflow.domain.process.config.ProcessChainProvider;
import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.rule.config.RuleChainProvider;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.types.RuleChainId;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/26 07:28
 **/
@Slf4j
@Component
@AllArgsConstructor
public class ConfigurationManager implements ProcessChainRepository, RuleChainRepository {

  private final Map<ProcessChainId, ProcessChain> processCache = new ConcurrentHashMap<>();
  private final Map<RuleChainId, RuleChain> ruleChainCache = new ConcurrentHashMap<>();

  private final List<ProcessChainProvider> processProviders;
  private final List<RuleChainProvider> ruleChainProviders;


  @PostConstruct
  public void initialize() {
    refreshProcesses();
    refreshRuleChains();
  }

  /**
   * 独立刷新流程定义缓存。
   */
  public void refreshProcesses() {
    log.info("Refreshing process definitions cache from all providers...");
    Map<ProcessChainId, ProcessChain> newDefinitions = processProviders.stream()
      .peek(provider -> log.info("Loading processes from source: {}", provider.getSourceName()))
      .flatMap(provider -> provider.loadProcessChains().stream())
      .collect(Collectors.toMap(
        ProcessChain::getId,
        Function.identity(),
        (existing, replacement) -> {
          log.warn("Duplicate ProcessChainId found: {}. The definition from a later provider will overwrite the existing one.", existing.id());
          return replacement;
        }
      ));

    processCache.clear();
    processCache.putAll(newDefinitions);
    log.info("Process definitions cache refreshed. Total loaded: {}", processCache.size());
  }

  /**
   * 独立刷新规则链定义缓存。
   */
  public void refreshRuleChains() {
    log.info("Refreshing rule chain definitions cache from all providers...");
    Map<RuleChainId, RuleChain> newDefinitions = ruleChainProviders.stream()
      .peek(provider -> log.info("Loading rule chains from source: {}", provider.getSourceName()))
      .flatMap(provider -> provider.loadRuleChainDefinitions().stream())
      .collect(Collectors.toMap(
        RuleChain::getId,
        Function.identity(),
        (existing, replacement) -> {
          log.warn("Duplicate RuleChainId found: {}. The definition from a later provider will overwrite the existing one.", existing.id());
          return replacement;
        }
      ));
    ruleChainCache.clear();
    ruleChainCache.putAll(newDefinitions);
    log.info("Rule chain definitions cache refreshed. Total loaded: {}", ruleChainCache.size());
  }

  @Override
  public Optional<ProcessChain> findById(ProcessChainId id) {
    return Optional.ofNullable(processCache.get(id));
  }

  @Override
  public Collection<ProcessChain> findAll() {
    return processCache.values();
  }

  @Override
  public ProcessChain save(ProcessChain aggregateRoot) {
    processCache.put(aggregateRoot.id(), aggregateRoot);
    log.info("Process definition {} saved/updated in cache.", aggregateRoot.id());
    // 注意：这只更新了缓存。如果需要持久化到DB，需要一个专门的DB写入服务。
    return aggregateRoot;
  }

  @Override
  public void delete(ProcessChain aggregateRoot) {
    deleteById(aggregateRoot.id());
  }

  @Override
  public void deleteById(ProcessChainId id) {
    processCache.remove(id);
    log.info("Process definition {} deleted from cache.", id);
  }

  // --- RuleChainDefinitionRepository 实现 ---
  @Override
  public Optional<RuleChainDefinition> findById(RuleChainId id) {
    return Optional.ofNullable(ruleChainCache.get(id));
  }

  @Override
  public Collection<RuleChainDefinition> findAll() {
    return ruleChainCache.values();
  }

  @Override
  public RuleChainDefinition save(RuleChainDefinition aggregateRoot) {
    ruleChainCache.put(aggregateRoot.id(), aggregateRoot);
    log.info("Rule chain definition {} saved/updated in cache.", aggregateRoot.id());
    return aggregateRoot;
  }

  @Override
  public void delete(RuleChainDefinition aggregateRoot) {
    deleteById(aggregateRoot.id());
  }

  @Override
  public void deleteById(RuleChainId id) {
    ruleChainCache.remove(id);
    log.info("Rule chain definition {} deleted from cache.", id);
  }


  // ... 实现两个Repository的接口方法 ...
}

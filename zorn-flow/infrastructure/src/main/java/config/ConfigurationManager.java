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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

  // 独立刷新流程定义
  public void refreshProcesses() {
    processCache.clear();
    processProviders.forEach(provider ->
      provider.loadProcessDefinitions().forEach(p -> processCache.put(p.id(), p))
    );
    log.info("Process definitions cache refreshed. Total loaded: {}", processCache.size());
  }

  // 独立刷新规则链定义
  public void refreshRuleChains() {
    ruleChainCache.clear();
    ruleChainProviders.forEach(provider ->
      provider.loadRuleChainDefinitions().forEach(r -> ruleChainCache.put(r.getId(), r))
    );
    log.info("Rule chain definitions cache refreshed. Total loaded: {}", ruleChainCache.size());
  }

  // ... 实现两个Repository的接口方法 ...
}

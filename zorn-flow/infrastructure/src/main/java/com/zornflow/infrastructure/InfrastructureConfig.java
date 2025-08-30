package com.zornflow.infrastructure;

import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import com.zornflow.infrastructure.config.repository.CompositeProcessChainRepository;
import com.zornflow.infrastructure.config.repository.CompositeRuleChainRepository;
import com.zornflow.infrastructure.presistence.serializer.BusinessContextJacksonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 基础设施层配置类
 * 配置Repository实现和配置源Bean
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/29 12:10
 */
@Slf4j
@Configuration
public class InfrastructureConfig {

  /**
   * 配置规则链仓库实现
   * 使用组合配置源作为数据源，修复类型匹配问题
   */
  @Bean
  @Primary
  @ConditionalOnMissingBean(RuleChainRepository.class)
  public RuleChainRepository ruleChainRepository(ReadWriteConfigSource configSource) {
    log.info("初始化组合规则链仓库，配置源: {}", configSource.getSourceName());
    return new CompositeRuleChainRepository(configSource);
  }

  /**
   * 配置流程链仓库实现
   * 使用组合配置源作为数据源，修复类型匹配问题
   */
  @Bean
  @Primary
  @ConditionalOnMissingBean(ProcessChainRepository.class)
  public ProcessChainRepository processChainRepository(ReadWriteConfigSource configSource) {
    log.info("初始化组合流程链仓库，配置源: {}", configSource.getSourceName());
    return new CompositeProcessChainRepository(configSource);
  }

  @Bean
  public BusinessContextJacksonSerializer businessContextSerializer() {
    return new BusinessContextJacksonSerializer();
  }
}

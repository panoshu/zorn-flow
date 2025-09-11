package com.zornflow.application.configuration;

import com.zornflow.domain.process.repository.ProcessChainRepository;
import com.zornflow.domain.process.service.GatewayProcessor;
import com.zornflow.domain.process.service.ProcessOrchestrationService;
import com.zornflow.domain.process.service.impl.DefaultProcessOrchestrationService;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.service.ConditionEvaluator;
import com.zornflow.domain.rule.service.HandlerExecutorFactory;
import com.zornflow.domain.rule.service.RuleChainExecutionService;
import com.zornflow.domain.rule.service.impl.DefaultRuleChainExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/8 03:17
 **/

@Configuration
public class ApplicationConfig {

  @Bean
  public RuleChainExecutionService ruleChainExecutionService(
    ConditionEvaluator conditionEvaluator,
    HandlerExecutorFactory handlerExecutorFactory
  ) {
    return new DefaultRuleChainExecutionService(conditionEvaluator, handlerExecutorFactory);
  }

  @Bean
  public ProcessOrchestrationService processOrchestrationService(
    ProcessChainRepository definitionRepository,
    RuleChainRepository ruleChainRepository,
    RuleChainExecutionService ruleChainExecutionService,
    GatewayProcessor gatewayProcessor) {
    return new DefaultProcessOrchestrationService(definitionRepository, ruleChainRepository, ruleChainExecutionService, gatewayProcessor);
  }
}

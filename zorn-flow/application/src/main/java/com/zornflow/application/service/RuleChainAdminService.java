package com.zornflow.application.service;

import com.zornflow.application.dto.rulechain.CreateRuleChainRequest;
import com.zornflow.application.dto.rulechain.RuleChainResponse;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.repository.RuleChainRepository;
import com.zornflow.domain.rule.types.*;
import com.zornflow.domain.rule.valueobject.Handler;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.mapper.RuleDomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleChainAdminService {

  private final RuleChainRepository ruleChainRepository;
  private final RuleDomainMapper ruleDomainMapper;

  @Transactional
  public RuleChainResponse createRuleChain(CreateRuleChainRequest request) {
    RuleChain ruleChain = RuleChain.builder()
      .id(RuleChainId.of(request.id()))
      .name(RuleChainName.of(request.name()))
      .description(request.description())
      .rules(toDomainRules(request.rules()))
      .build();

    RuleChain saved = ruleChainRepository.save(ruleChain);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public Optional<RuleChainResponse> getRuleChainById(String id) {
    return ruleChainRepository.findById(RuleChainId.of(id)).map(this::toResponse);
  }

  @Transactional
  public void deleteRuleChain(String id) {
    ruleChainRepository.deleteById(RuleChainId.of(id));
  }

  // --- Helper Methods ---

  private RuleChainResponse toResponse(RuleChain ruleChain) {
    return new RuleChainResponse(
      ruleChain.getId().value(),
      ruleChain.getName().value(),
      ruleChain.getDescription(),
      ruleChain.getVersion(),
      ruleChain.getRules().stream().map(ruleDomainMapper::toDto).collect(Collectors.toList()),
      ruleChain.getCreatedAt(),
      ruleChain.getUpdatedAt()
    );
  }

  private List<Rule> toDomainRules(List<RuleConfig> ruleConfigs) {
    return ruleConfigs.stream()
      .map(this::toDomainRule)
      .collect(Collectors.toList());
  }

  private Rule toDomainRule(RuleConfig dto) {
    return Rule.builder()
      .id(RuleId.of(dto.id()))
      .name(RuleName.of(dto.name()))
      .priority(Priority.of(dto.priority()))
      .condition(Condition.of(dto.condition()))
      .handler(new Handler(
        HandlerType.valueOf(dto.handle().type().name()),
        dto.handle().handler(),
        dto.handle().parameters()
      ))
      .build();
  }
}

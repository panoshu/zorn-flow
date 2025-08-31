package com.zornflow.infrastructure.repository;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.*;
import com.zornflow.domain.rule.valueobject.Handler;
import com.zornflow.infrastructure.config.mapper.RuleConfigMapper;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.config.repository.CompositeRuleChainRepository;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CompositeRuleChainRepository 单元测试
 * 基于 MapStruct RuleConfigMapper
 */
@ExtendWith(MockitoExtension.class)
class CompositeRuleChainRepo {

  @Mock
  private ReadWriteConfigSource readWriteConfigSource;

  @Mock
  private RuleConfigMapper mapper;   // 直接 mock mapper，避免 MockedStatic

  @InjectMocks
  private CompositeRuleChainRepository repository;

  private RuleId ruleId;
  private Rule rule;
  private RuleConfig ruleConfig;
  private RuleChainId ruleChainId;
  private RuleChain ruleChain;
  private RuleChainConfig chainConfig;

  @BeforeEach
  void setUp() {
    ruleId = RuleId.of("rule1");
    rule = Rule.builder()
      .id(ruleId)
      .name(RuleName.of(ruleId))
      .condition(Condition.of("#{id=1}"))
      .handler(Handler.of(HandlerType.CLASS, "com.example.handler"))
      .build();

    ruleConfig =  RuleConfig.builder()
      .id("rule1")
      .name("rule1")
      .condition("#{id=1}")
      .handlerConfig(new RuleConfig.HandlerConfig(RuleConfig.HandlerConfig.Type.CLASS, "com.example.handler",Map.of()))
      .build();

    ruleChainId = RuleChainId.of("test-chain");
    ruleChain = RuleChain.builder()
      .id(ruleChainId)
      .name(RuleChainName.of(ruleChainId))
      .version(Version.defaultVersion())
      .rules(List.of(rule))
      .build();

    chainConfig = RuleChainConfig.builder()
      .id("test-chain")
      .name("test-chain")
      .version("1.0.1")
      .ruleConfigs(List.of(ruleConfig))
      .build();
  }

  /* ---------- findById ---------- */

  @Test
  void findById_shouldReturnEmpty_whenIdNull() {
    Optional<RuleChain> result = repository.findById(null);
    assertThat(result).isEmpty();
    verifyNoInteractions(readWriteConfigSource);
  }

  @Test
  void findById_shouldReturnEmpty_whenConfigNotFound() {
    when(readWriteConfigSource.loadRuleChainConfig("test-chain"))
      .thenReturn(Optional.empty());

    Optional<RuleChain> result = repository.findById(ruleChainId);
    assertThat(result).isEmpty();
  }

  @Test
  void findById_shouldReturnRuleChain_whenConfigExists() {
    when(readWriteConfigSource.loadRuleChainConfig("test-chain"))
      .thenReturn(Optional.of(chainConfig));
    when(mapper.toRuleChain(chainConfig)).thenReturn(ruleChain);

    Optional<RuleChain> result = repository.findById(ruleChainId);
    assertThat(result).containsSame(ruleChain);
  }

  @Test
  void findById_shouldReturnEmpty_whenExceptionThrown() {
    when(readWriteConfigSource.loadRuleChainConfig("test-chain"))
      .thenThrow(new RuntimeException("boom"));

    Optional<RuleChain> result = repository.findById(ruleChainId);
    assertThat(result).isEmpty();
  }

  /* ---------- findAll ---------- */

  @Test
  void findAll_shouldReturnEmpty_whenNoConfigs() {
    when(readWriteConfigSource.loadRuleChainConfigs()).thenReturn(Collections.emptyMap());

    Collection<RuleChain> result = repository.findAll();
    assertThat(result).isEmpty();
  }

  @Test
  void findAll_shouldReturnRuleChains_whenConfigsExist() {
    RuleChainConfig cfg1 = RuleChainConfig.builder()
      .id("cfg1")
      .name("cfg1")
      .ruleConfigs(List.of(ruleConfig))
      .version("1.0.0")
      .build();
    RuleChainConfig cfg2 = RuleChainConfig.builder()
      .id("cfg2")
      .name("cfg2")
      .ruleConfigs(List.of(ruleConfig))
      .version("1.0.0")
      .build();

    when(readWriteConfigSource.loadRuleChainConfigs())
      .thenReturn(Map.of("c1", cfg1, "c2", cfg2));

    when(mapper.toRuleChain(cfg1)).thenReturn(ruleChain);
    when(mapper.toRuleChain(cfg2)).thenReturn(ruleChain);

    Collection<RuleChain> result = repository.findAll();
    assertThat(result).hasSize(2);
  }

  /* ---------- save ---------- */

  @Test
  void save_shouldPersistConfig_andReturnAggregate() {
    when(mapper.toRuleChainConfig(ruleChain)).thenReturn(chainConfig);

    RuleChain saved = repository.save(ruleChain);

    assertThat(saved).isSameAs(ruleChain);
    verify(readWriteConfigSource).saveRuleChainConfig(chainConfig);
  }

  @Test
  void save_shouldThrow_whenExceptionOccurs() {
    when(mapper.toRuleChainConfig(ruleChain)).thenReturn(chainConfig);
    doThrow(new RuntimeException("save failed"))
      .when(readWriteConfigSource).saveRuleChainConfig(chainConfig);

    assertThatThrownBy(() -> repository.save(ruleChain))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("保存规则链失败");
  }

  /* ---------- delete ---------- */

  @Test
  void delete_shouldCallDeleteById() {
    repository.delete(ruleChain);
    verify(readWriteConfigSource).deleteRuleChainConfig("test-chain");
  }

  /* ---------- refresh ---------- */

  @Test
  void refresh_shouldReturnTrue_whenSuccess() {
    when(readWriteConfigSource.refresh()).thenReturn(true);
    assertThat(repository.refresh()).isTrue();
  }

  /* ---------- clearCache ---------- */

  @Test
  void clearCache_shouldDelegateToSource() {
    repository.clearCache();
    verify(readWriteConfigSource).clearCache();
  }

  /* ---------- isAvailable ---------- */

  @Test
  void isAvailable_shouldDelegateToSource() {
    when(readWriteConfigSource.isAvailable()).thenReturn(false);
    assertThat(repository.isAvailable()).isFalse();
  }
}

package com.zornflow.infrastructure.repository;

import com.zornflow.domain.common.types.Version;
import com.zornflow.domain.rule.entity.Rule;
import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.HandlerType;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.domain.rule.types.RuleId;
import com.zornflow.domain.rule.valueobject.Handler;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.config.source.cache.CachingRuleChainCompositeConfigSourceDecorator;
import com.zornflow.infrastructure.repository.mapper.RuleConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleChainRepositoryImplTest {

  @Mock
  private CachingRuleChainCompositeConfigSourceDecorator configSource;
  @Mock
  private RuleConfigMapper mapper;

  @InjectMocks
  private RuleChainRepositoryImpl ruleChainRepository;

  private RuleChainId ruleChainId;
  private RuleChain domainEntity;
  private RuleChainConfig dto;

  @BeforeEach
  void setUp() {
    ruleChainId = RuleChainId.of("chain-1");

    // FIX: Provide all required fields for the builders to avoid NPE
    domainEntity = RuleChain.builder()
      .id(ruleChainId)
      .version(Version.of("1.0.0"))
      .rules(List.of(Rule.builder()
        .id(RuleId.of("rule-1"))
        .handler(Handler.of(HandlerType.CLASS, "SomeHandler")) // Required field
        .build()))
      .build();

    dto = RuleChainConfig.builder()
      .id("chain-1")
      .version("1.0.0")
      .rules(List.of(RuleConfig.builder().id("rule-1").build()))
      .build();
  }

  @Test
  @DisplayName("findById should return domain entity when config source finds DTO")
  void findById_shouldReturnDomainEntity_whenDtoIsFound() throws IOException {
    when(configSource.load("chain-1")).thenReturn(Optional.of(dto));
    when(mapper.toDomain(dto)).thenReturn(domainEntity);
    Optional<RuleChain> result = ruleChainRepository.findById(ruleChainId);
    assertTrue(result.isPresent());
    assertEquals(domainEntity, result.get());
  }

  @Test
  @DisplayName("findById should return empty optional when config source finds nothing")
  void findById_shouldReturnEmpty_whenDtoIsNotFound() throws IOException {
    when(configSource.load("chain-1")).thenReturn(Optional.empty());
    Optional<RuleChain> result = ruleChainRepository.findById(ruleChainId);
    assertFalse(result.isPresent());
    verify(mapper, never()).toDomain(any(RuleChainConfig.class));
  }

  @Test
  @DisplayName("findAll should return a list of all domain entities")
  void findAll_shouldReturnListOfDomainEntities() throws IOException {
    when(configSource.loadAll()).thenReturn(Map.of("chain-1", dto));
    when(mapper.toDomain(dto)).thenReturn(domainEntity);
    List<RuleChain> results = ruleChainRepository.findAll();
    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("save should map entity to DTO, save via source, and return mapped domain entity")
  void save_shouldMapAndSaveAndReturnEntity() throws IOException {
    when(mapper.toDto(domainEntity)).thenReturn(dto);
    when(configSource.save(dto)).thenReturn(Optional.of(dto));
    when(mapper.toDomain(dto)).thenReturn(domainEntity);
    RuleChain savedEntity = ruleChainRepository.save(domainEntity);
    assertNotNull(savedEntity);
  }

  @Test
  @DisplayName("save should throw IllegalStateException if config source fails to return saved entity")
  void save_shouldThrowException_whenSaveReturnsEmpty() throws IOException {
    when(mapper.toDto(domainEntity)).thenReturn(dto);
    when(configSource.save(dto)).thenReturn(Optional.empty());
    assertThrows(IllegalStateException.class, () -> ruleChainRepository.save(domainEntity));
  }

  @Test
  @DisplayName("deleteById should call config source's delete method")
  void deleteById_shouldCallConfigSourceDelete() {
    ruleChainRepository.deleteById(ruleChainId);
    verify(configSource).delete("chain-1");
  }
}

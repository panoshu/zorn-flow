package com.zornflow.infrastructure.repository;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.entity.ProcessNode;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.domain.process.types.ProcessNodeId;
import com.zornflow.domain.process.valueobject.NodeType;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.source.cache.CachingProcessChainCompositeConfigSourceDecorator;
import com.zornflow.infrastructure.mapper.ProcessDomainMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessChainRepositoryImplTest {

  @Mock
  private CachingProcessChainCompositeConfigSourceDecorator configSource;
  @Mock
  private ProcessDomainMapper mapper;

  @InjectMocks
  private ProcessChainRepositoryImpl processChainRepository;

  private ProcessChainId processChainId;
  private ProcessChain domainEntity;
  private ProcessChainConfig dto;

  @BeforeEach
  void setUp() {
    processChainId = ProcessChainId.of("proc-1");

    // FIX: Provide all required fields for the builders, including empty collections.
    ProcessNode node = ProcessNode.builder()
      .id(ProcessNodeId.of("node-1"))
      .type(NodeType.BUSINESS)
      .ruleChainId(RuleChainId.of("rc-1"))
      .properties(Collections.emptyMap()) // FIX: Added this
      .conditions(Collections.emptyList()) // FIX: Added this
      .build();

    domainEntity = ProcessChain.builder()
      .id(processChainId)
      .nodes(List.of(node))
      .build();

    dto = ProcessChainConfig.builder()
      .id("proc-1")
      .version(0)
      .nodes(List.of(ProcessNodeConfig.builder().id("node-1").build()))
      .build();
  }

  @Test
  @DisplayName("findById should return domain entity when DTO is found")
  void findById_shouldReturnDomainEntity() {
    when(configSource.load("proc-1")).thenReturn(Optional.of(dto));
    when(mapper.toDomain(dto)).thenReturn(domainEntity);
    Optional<ProcessChain> result = processChainRepository.findById(processChainId);
    assertTrue(result.isPresent());
  }

  @Test
  @DisplayName("findAll should return all domain entities")
  void findAll_shouldReturnAllDomainEntities() {
    when(configSource.loadAll()).thenReturn(Map.of("proc-1", dto));
    when(mapper.toDomain(dto)).thenReturn(domainEntity);
    List<ProcessChain> results = processChainRepository.findAll();
    assertFalse(results.isEmpty());
  }

  @Test
  @DisplayName("save should correctly map, save, and return the domain entity")
  void save_shouldMapAndSaveAndReturn() throws IOException {
    when(mapper.toDto(domainEntity)).thenReturn(dto);
    when(configSource.save(dto)).thenReturn(Optional.of(dto));
    when(mapper.toDomain(dto)).thenReturn(domainEntity);
    ProcessChain savedEntity = processChainRepository.save(domainEntity);
    assertNotNull(savedEntity);
  }

  @Test
  @DisplayName("deleteById should call the config source's delete method")
  void deleteById_shouldCallConfigSource() {
    processChainRepository.deleteById(processChainId);
    verify(configSource).delete("proc-1");
  }
}

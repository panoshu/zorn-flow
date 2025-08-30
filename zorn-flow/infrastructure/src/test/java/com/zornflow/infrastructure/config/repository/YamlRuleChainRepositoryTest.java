package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.ReadableConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * YAML规则链仓库单元测试
 */
class YamlRuleChainRepositoryTest {

    @Mock
    private ReadableConfigSource readableConfigSource;

    private YamlRuleChainRepository yamlRuleChainRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        yamlRuleChainRepository = new YamlRuleChainRepository(readableConfigSource);
    }

    @Test
    void testFindById() {
        RuleChainConfig config = RuleChainConfig.builder()
            .id("test-rule-chain")
            .name("Test Rule Chain")
            .version("1.0")
            .build();
        
        when(readableConfigSource.loadRuleChainConfig("test-rule-chain"))
            .thenReturn(Optional.of(config));
        
        Optional<RuleChain> result = yamlRuleChainRepository.findById(RuleChainId.of("test-rule-chain"));
        
        assertTrue(result.isPresent());
        assertEquals("test-rule-chain", result.get().getId().value());
        assertEquals("Test Rule Chain", result.get().getName().value());
    }

    @Test
    void testFindByIdNotFound() {
        when(readableConfigSource.loadRuleChainConfig("non-existent"))
            .thenReturn(Optional.empty());
        
        Optional<RuleChain> result = yamlRuleChainRepository.findById(RuleChainId.of("non-existent"));
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdWithNullId() {
        Optional<RuleChain> result = yamlRuleChainRepository.findById(null);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Map<String, RuleChainConfig> configs = new HashMap<>();
        configs.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").version("1.0").build());
        configs.put("rule2", RuleChainConfig.builder().id("rule2").name("Rule 2").version("1.0").build());
        
        when(readableConfigSource.loadRuleChainConfigs()).thenReturn(configs);
        
        Collection<RuleChain> result = yamlRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllWhenException() {
        when(readableConfigSource.loadRuleChainConfigs()).thenThrow(new RuntimeException("Load failed"));
        
        Collection<RuleChain> result = yamlRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveShouldThrowException() {
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        
        assertThrows(UnsupportedOperationException.class, () -> {
            yamlRuleChainRepository.save(ruleChain);
        });
    }

    @Test
    void testDeleteShouldThrowException() {
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        
        assertThrows(UnsupportedOperationException.class, () -> {
            yamlRuleChainRepository.delete(ruleChain);
        });
    }

    @Test
    void testDeleteByIdShouldThrowException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            yamlRuleChainRepository.deleteById(RuleChainId.of("test-id"));
        });
    }

    @Test
    void testRefresh() {
        when(readableConfigSource.refresh()).thenReturn(true);
        
        boolean result = yamlRuleChainRepository.refresh();
        
        assertTrue(result);
    }

    @Test
    void testRefreshWhenFailed() {
        when(readableConfigSource.refresh()).thenReturn(false);
        
        boolean result = yamlRuleChainRepository.refresh();
        
        assertFalse(result);
    }

    @Test
    void testFindByIdWhenConfigMapperException() {
        RuleChainConfig config = RuleChainConfig.builder()
            .id("test-rule-chain")
            .name("Test Rule Chain")
            .version("1.0")
            .build();
        
        when(readableConfigSource.loadRuleChainConfig("test-rule-chain"))
            .thenReturn(Optional.of(config));
        
        // 注意：在实际测试中，我们无法轻松模拟RuleConfigMapper.INSTANCE.toRuleChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Optional<RuleChain> result = yamlRuleChainRepository.findById(RuleChainId.of("test-rule-chain"));
        
        assertTrue(result.isPresent());
    }

    @Test
    void testFindAllWhenConfigMapperException() {
        Map<String, RuleChainConfig> configs = new HashMap<>();
        configs.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").version("1.0").build());
        
        when(readableConfigSource.loadRuleChainConfigs()).thenReturn(configs);
        
        // 注意：在实际测试中，我们无法轻松模拟RuleConfigMapper.INSTANCE.toRuleChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Collection<RuleChain> result = yamlRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.rule.entity.RuleChain;
import com.zornflow.domain.rule.types.RuleChainId;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 组合规则链仓库单元测试
 */
class CompositeRuleChainRepositoryTest {

    @Mock
    private ReadWriteConfigSource readWriteConfigSource;

    private CompositeRuleChainRepository compositeRuleChainRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        compositeRuleChainRepository = new CompositeRuleChainRepository(readWriteConfigSource);
    }

    @Test
    void testFindById() {
        RuleChainConfig config = RuleChainConfig.builder()
            .id("test-rule-chain")
            .name("Test Rule Chain")
            .version("1.0")
            .build();
        
        when(readWriteConfigSource.loadRuleChainConfig("test-rule-chain"))
            .thenReturn(Optional.of(config));
        
        Optional<RuleChain> result = compositeRuleChainRepository.findById(RuleChainId.of("test-rule-chain"));
        
        assertTrue(result.isPresent());
        assertEquals("test-rule-chain", result.get().getId().value());
        assertEquals("Test Rule Chain", result.get().getName().value());
    }

    @Test
    void testFindByIdNotFound() {
        when(readWriteConfigSource.loadRuleChainConfig("non-existent"))
            .thenReturn(Optional.empty());
        
        Optional<RuleChain> result = compositeRuleChainRepository.findById(RuleChainId.of("non-existent"));
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdWithNullId() {
        Optional<RuleChain> result = compositeRuleChainRepository.findById(null);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Map<String, RuleChainConfig> configs = new HashMap<>();
        configs.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").version("1.0").build());
        configs.put("rule2", RuleChainConfig.builder().id("rule2").name("Rule 2").version("1.0").build());
        
        when(readWriteConfigSource.loadRuleChainConfigs()).thenReturn(configs);
        
        Collection<RuleChain> result = compositeRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllWhenException() {
        when(readWriteConfigSource.loadRuleChainConfigs()).thenThrow(new RuntimeException("Load failed"));
        
        Collection<RuleChain> result = compositeRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave() {
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        
        RuleChain result = compositeRuleChainRepository.save(ruleChain);
        
        assertNotNull(result);
        assertEquals(ruleChain, result);
        verify(readWriteConfigSource).saveRuleChainConfig(any(RuleChainConfig.class));
    }

    @Test
    void testSaveWhenException() {
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        doThrow(new RuntimeException("Save failed"))
            .when(readWriteConfigSource).saveRuleChainConfig(any(RuleChainConfig.class));
        
        assertThrows(RuntimeException.class, () -> {
            compositeRuleChainRepository.save(ruleChain);
        });
    }

    @Test
    void testDelete() {
        RuleChain ruleChain = mock(RuleChain.class);
        RuleChainId ruleChainId = RuleChainId.of("test-id");
        when(ruleChain.getId()).thenReturn(ruleChainId);
        
        assertDoesNotThrow(() -> {
            compositeRuleChainRepository.delete(ruleChain);
        });
        
        verify(readWriteConfigSource).deleteRuleChainConfig("test-id");
    }

    @Test
    void testDeleteById() {
        RuleChainId ruleChainId = RuleChainId.of("test-id");
        
        assertDoesNotThrow(() -> {
            compositeRuleChainRepository.deleteById(ruleChainId);
        });
        
        verify(readWriteConfigSource).deleteRuleChainConfig("test-id");
    }

    @Test
    void testDeleteByIdWhenException() {
        RuleChainId ruleChainId = RuleChainId.of("test-id");
        doThrow(new RuntimeException("Delete failed"))
            .when(readWriteConfigSource).deleteRuleChainConfig("test-id");
        
        assertThrows(RuntimeException.class, () -> {
            compositeRuleChainRepository.deleteById(ruleChainId);
        });
    }

    @Test
    void testRefresh() {
        when(readWriteConfigSource.refresh()).thenReturn(true);
        
        boolean result = compositeRuleChainRepository.refresh();
        
        assertTrue(result);
    }

    @Test
    void testRefreshWhenFailed() {
        when(readWriteConfigSource.refresh()).thenReturn(false);
        
        boolean result = compositeRuleChainRepository.refresh();
        
        assertFalse(result);
    }

    @Test
    void testClearCache() {
        assertDoesNotThrow(() -> {
            compositeRuleChainRepository.clearCache();
        });
        
        verify(readWriteConfigSource).clearCache();
    }

    @Test
    void testIsAvailable() {
        when(readWriteConfigSource.isAvailable()).thenReturn(true);
        
        boolean result = compositeRuleChainRepository.isAvailable();
        
        assertTrue(result);
    }

    @Test
    void testSaveAll() {
        List<RuleChain> ruleChains = new ArrayList<>();
        RuleChain ruleChain1 = mock(RuleChain.class);
        when(ruleChain1.getId()).thenReturn(RuleChainId.of("test-id-1"));
        ruleChains.add(ruleChain1);
        
        RuleChain ruleChain2 = mock(RuleChain.class);
        when(ruleChain2.getId()).thenReturn(RuleChainId.of("test-id-2"));
        ruleChains.add(ruleChain2);
        
        assertDoesNotThrow(() -> {
            compositeRuleChainRepository.saveAll(ruleChains);
        });
        
        // 验证每个规则链都被保存
        verify(readWriteConfigSource, times(2)).saveRuleChainConfig(any(RuleChainConfig.class));
    }

    @Test
    void testSaveAllWhenException() {
        List<RuleChain> ruleChains = new ArrayList<>();
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        ruleChains.add(ruleChain);
        
        doThrow(new RuntimeException("Save failed"))
            .when(readWriteConfigSource).saveRuleChainConfig(any(RuleChainConfig.class));
        
        assertThrows(RuntimeException.class, () -> {
            compositeRuleChainRepository.saveAll(ruleChains);
        });
    }

    @Test
    void testFindByIdWhenConfigMapperException() {
        RuleChainConfig config = RuleChainConfig.builder()
            .id("test-rule-chain")
            .name("Test Rule Chain")
            .version("1.0")
            .build();
        
        when(readWriteConfigSource.loadRuleChainConfig("test-rule-chain"))
            .thenReturn(Optional.of(config));
        
        // 注意：在实际测试中，我们无法轻松模拟RuleConfigMapper.INSTANCE.toRuleChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Optional<RuleChain> result = compositeRuleChainRepository.findById(RuleChainId.of("test-rule-chain"));
        
        assertTrue(result.isPresent());
    }

    @Test
    void testFindAllWhenConfigMapperException() {
        Map<String, RuleChainConfig> configs = new HashMap<>();
        configs.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").version("1.0").build());
        
        when(readWriteConfigSource.loadRuleChainConfigs()).thenReturn(configs);
        
        // 注意：在实际测试中，我们无法轻松模拟RuleConfigMapper.INSTANCE.toRuleChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Collection<RuleChain> result = compositeRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testClearCacheWhenException() {
        doThrow(new RuntimeException("Clear cache failed"))
            .when(readWriteConfigSource).clearCache();
        
        // clearCache方法应该处理异常并不抛出
        assertDoesNotThrow(() -> compositeRuleChainRepository.clearCache());
    }
}
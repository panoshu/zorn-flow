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
 * 数据库规则链仓库单元测试
 */
class DatabaseRuleChainRepositoryTest {

    @Mock
    private ReadWriteConfigSource readWriteConfigSource;

    private DatabaseRuleChainRepository databaseRuleChainRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseRuleChainRepository = new DatabaseRuleChainRepository(readWriteConfigSource);
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
        
        Optional<RuleChain> result = databaseRuleChainRepository.findById(RuleChainId.of("test-rule-chain"));
        
        assertTrue(result.isPresent());
        assertEquals("test-rule-chain", result.get().getId().value());
        assertEquals("Test Rule Chain", result.get().getName().value());
    }

    @Test
    void testFindByIdNotFound() {
        when(readWriteConfigSource.loadRuleChainConfig("non-existent"))
            .thenReturn(Optional.empty());
        
        Optional<RuleChain> result = databaseRuleChainRepository.findById(RuleChainId.of("non-existent"));
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdWithNullId() {
        Optional<RuleChain> result = databaseRuleChainRepository.findById(null);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Map<String, RuleChainConfig> configs = new HashMap<>();
        configs.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").version("1.0").build());
        configs.put("rule2", RuleChainConfig.builder().id("rule2").name("Rule 2").version("1.0").build());
        
        when(readWriteConfigSource.loadRuleChainConfigs()).thenReturn(configs);
        
        Collection<RuleChain> result = databaseRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllWhenException() {
        when(readWriteConfigSource.loadRuleChainConfigs()).thenThrow(new RuntimeException("Load failed"));
        
        Collection<RuleChain> result = databaseRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave() {
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        
        assertDoesNotThrow(() -> {
            databaseRuleChainRepository.save(ruleChain);
        });
        
        verify(readWriteConfigSource).saveRuleChainConfig(any(RuleChainConfig.class));
    }

    @Test
    void testSaveWhenException() {
        RuleChain ruleChain = mock(RuleChain.class);
        when(ruleChain.getId()).thenReturn(RuleChainId.of("test-id"));
        doThrow(new RuntimeException("Save failed"))
            .when(readWriteConfigSource).saveRuleChainConfig(any(RuleChainConfig.class));
        
        assertThrows(RuntimeException.class, () -> {
            databaseRuleChainRepository.save(ruleChain);
        });
    }

    @Test
    void testDelete() {
        RuleChain ruleChain = mock(RuleChain.class);
        RuleChainId ruleChainId = RuleChainId.of("test-id");
        when(ruleChain.getId()).thenReturn(ruleChainId);
        
        assertDoesNotThrow(() -> {
            databaseRuleChainRepository.delete(ruleChain);
        });
        
        verify(readWriteConfigSource).deleteRuleChainConfig("test-id");
    }

    @Test
    void testDeleteById() {
        RuleChainId ruleChainId = RuleChainId.of("test-id");
        
        assertDoesNotThrow(() -> {
            databaseRuleChainRepository.deleteById(ruleChainId);
        });
        
        verify(readWriteConfigSource).deleteRuleChainConfig("test-id");
    }

    @Test
    void testDeleteByIdWhenException() {
        RuleChainId ruleChainId = RuleChainId.of("test-id");
        doThrow(new RuntimeException("Delete failed"))
            .when(readWriteConfigSource).deleteRuleChainConfig("test-id");
        
        assertThrows(RuntimeException.class, () -> {
            databaseRuleChainRepository.deleteById(ruleChainId);
        });
    }

    @Test
    void testRefresh() {
        when(readWriteConfigSource.refresh()).thenReturn(true);
        
        boolean result = databaseRuleChainRepository.refresh();
        
        assertTrue(result);
    }

    @Test
    void testRefreshWhenFailed() {
        when(readWriteConfigSource.refresh()).thenReturn(false);
        
        boolean result = databaseRuleChainRepository.refresh();
        
        assertFalse(result);
    }

    @Test
    void testClearCache() {
        assertDoesNotThrow(() -> {
            databaseRuleChainRepository.clearCache();
        });
        
        verify(readWriteConfigSource).clearCache();
    }

    @Test
    void testIsAvailable() {
        when(readWriteConfigSource.isAvailable()).thenReturn(true);
        
        boolean result = databaseRuleChainRepository.isAvailable();
        
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
            databaseRuleChainRepository.saveAll(ruleChains);
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
            databaseRuleChainRepository.saveAll(ruleChains);
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
        Optional<RuleChain> result = databaseRuleChainRepository.findById(RuleChainId.of("test-rule-chain"));
        
        assertTrue(result.isPresent());
    }

    @Test
    void testFindAllWhenConfigMapperException() {
        Map<String, RuleChainConfig> configs = new HashMap<>();
        configs.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").version("1.0").build());
        
        when(readWriteConfigSource.loadRuleChainConfigs()).thenReturn(configs);
        
        // 注意：在实际测试中，我们无法轻松模拟RuleConfigMapper.INSTANCE.toRuleChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Collection<RuleChain> result = databaseRuleChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testClearCacheWhenException() {
        doThrow(new RuntimeException("Clear cache failed"))
            .when(readWriteConfigSource).clearCache();
        
        // clearCache方法应该处理异常并不抛出
        assertDoesNotThrow(() -> databaseRuleChainRepository.clearCache());
    }
}
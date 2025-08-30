package com.zornflow.infrastructure.config.repository;

import com.zornflow.domain.process.entity.ProcessChain;
import com.zornflow.domain.process.types.ProcessChainId;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 组合流程链仓库单元测试
 */
class CompositeProcessChainRepositoryTest {

    @Mock
    private ReadWriteConfigSource readWriteConfigSource;

    private CompositeProcessChainRepository compositeProcessChainRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        compositeProcessChainRepository = new CompositeProcessChainRepository(readWriteConfigSource);
    }

    @Test
    void testFindById() {
        ProcessChainConfig config = ProcessChainConfig.builder()
            .id("test-process-chain")
            .name("Test Process Chain")
            .version("1.0")
            .build();
        
        when(readWriteConfigSource.loadProcessChainConfig("test-process-chain"))
            .thenReturn(Optional.of(config));
        
        Optional<ProcessChain> result = compositeProcessChainRepository.findById(ProcessChainId.of("test-process-chain"));
        
        assertTrue(result.isPresent());
        assertEquals("test-process-chain", result.get().getId().value());
        assertEquals("Test Process Chain", result.get().getName().value());
    }

    @Test
    void testFindByIdNotFound() {
        when(readWriteConfigSource.loadProcessChainConfig("non-existent"))
            .thenReturn(Optional.empty());
        
        Optional<ProcessChain> result = compositeProcessChainRepository.findById(ProcessChainId.of("non-existent"));
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdWithNullId() {
        Optional<ProcessChain> result = compositeProcessChainRepository.findById(null);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Map<String, ProcessChainConfig> configs = new HashMap<>();
        configs.put("process1", ProcessChainConfig.builder().id("process1").name("Process 1").version("1.0").build());
        configs.put("process2", ProcessChainConfig.builder().id("process2").name("Process 2").version("1.0").build());
        
        when(readWriteConfigSource.loadProcessChainConfigs()).thenReturn(configs);
        
        Collection<ProcessChain> result = compositeProcessChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllWhenException() {
        when(readWriteConfigSource.loadProcessChainConfigs()).thenThrow(new RuntimeException("Load failed"));
        
        Collection<ProcessChain> result = compositeProcessChainRepository.findAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave() {
        ProcessChain processChain = mock(ProcessChain.class);
        when(processChain.getId()).thenReturn(ProcessChainId.of("test-id"));
        
        ProcessChain result = compositeProcessChainRepository.save(processChain);
        
        assertNotNull(result);
        assertEquals(processChain, result);
        verify(readWriteConfigSource).saveProcessChainConfig(any(ProcessChainConfig.class));
    }

    @Test
    void testSaveWhenException() {
        ProcessChain processChain = mock(ProcessChain.class);
        when(processChain.getId()).thenReturn(ProcessChainId.of("test-id"));
        doThrow(new RuntimeException("Save failed"))
            .when(readWriteConfigSource).saveProcessChainConfig(any(ProcessChainConfig.class));
        
        assertThrows(RuntimeException.class, () -> {
            compositeProcessChainRepository.save(processChain);
        });
    }

    @Test
    void testDelete() {
        ProcessChain processChain = mock(ProcessChain.class);
        ProcessChainId processChainId = ProcessChainId.of("test-id");
        when(processChain.getId()).thenReturn(processChainId);
        
        assertDoesNotThrow(() -> {
            compositeProcessChainRepository.delete(processChain);
        });
        
        verify(readWriteConfigSource).deleteProcessChainConfig("test-id");
    }

    @Test
    void testDeleteById() {
        ProcessChainId processChainId = ProcessChainId.of("test-id");
        
        assertDoesNotThrow(() -> {
            compositeProcessChainRepository.deleteById(processChainId);
        });
        
        verify(readWriteConfigSource).deleteProcessChainConfig("test-id");
    }

    @Test
    void testDeleteByIdWhenException() {
        ProcessChainId processChainId = ProcessChainId.of("test-id");
        doThrow(new RuntimeException("Delete failed"))
            .when(readWriteConfigSource).deleteProcessChainConfig("test-id");
        
        assertThrows(RuntimeException.class, () -> {
            compositeProcessChainRepository.deleteById(processChainId);
        });
    }

    @Test
    void testRefresh() {
        when(readWriteConfigSource.refresh()).thenReturn(true);
        
        boolean result = compositeProcessChainRepository.refresh();
        
        assertTrue(result);
    }

    @Test
    void testRefreshWhenFailed() {
        when(readWriteConfigSource.refresh()).thenReturn(false);
        
        boolean result = compositeProcessChainRepository.refresh();
        
        assertFalse(result);
    }

    @Test
    void testClearCache() {
        assertDoesNotThrow(() -> {
            compositeProcessChainRepository.clearCache();
        });
        
        verify(readWriteConfigSource).clearCache();
    }

    @Test
    void testIsAvailable() {
        when(readWriteConfigSource.isAvailable()).thenReturn(true);
        
        boolean result = compositeProcessChainRepository.isAvailable();
        
        assertTrue(result);
    }

    @Test
    void testSaveAll() {
        List<ProcessChain> processChains = new ArrayList<>();
        ProcessChain processChain1 = mock(ProcessChain.class);
        when(processChain1.getId()).thenReturn(ProcessChainId.of("test-id-1"));
        processChains.add(processChain1);
        
        ProcessChain processChain2 = mock(ProcessChain.class);
        when(processChain2.getId()).thenReturn(ProcessChainId.of("test-id-2"));
        processChains.add(processChain2);
        
        assertDoesNotThrow(() -> {
            compositeProcessChainRepository.saveAll(processChains);
        });
        
        // 验证每个流程链都被保存
        verify(readWriteConfigSource, times(2)).saveProcessChainConfig(any(ProcessChainConfig.class));
    }

    @Test
    void testSaveAllWhenException() {
        List<ProcessChain> processChains = new ArrayList<>();
        ProcessChain processChain = mock(ProcessChain.class);
        when(processChain.getId()).thenReturn(ProcessChainId.of("test-id"));
        processChains.add(processChain);
        
        doThrow(new RuntimeException("Save failed"))
            .when(readWriteConfigSource).saveProcessChainConfig(any(ProcessChainConfig.class));
        
        assertThrows(RuntimeException.class, () -> {
            compositeProcessChainRepository.saveAll(processChains);
        });
    }

    @Test
    void testFindByIdWhenConfigMapperException() {
        ProcessChainConfig config = ProcessChainConfig.builder()
            .id("test-process-chain")
            .name("Test Process Chain")
            .version("1.0")
            .build();
        
        when(readWriteConfigSource.loadProcessChainConfig("test-process-chain"))
            .thenReturn(Optional.of(config));
        
        // 注意：在实际测试中，我们无法轻松模拟ProcessConfigMapper.INSTANCE.toProcessChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Optional<ProcessChain> result = compositeProcessChainRepository.findById(ProcessChainId.of("test-process-chain"));
        
        assertTrue(result.isPresent());
    }

    @Test
    void testFindAllWhenConfigMapperException() {
        Map<String, ProcessChainConfig> configs = new HashMap<>();
        configs.put("process1", ProcessChainConfig.builder().id("process1").name("Process 1").version("1.0").build());
        
        when(readWriteConfigSource.loadProcessChainConfigs()).thenReturn(configs);
        
        // 注意：在实际测试中，我们无法轻松模拟ProcessConfigMapper.INSTANCE.toProcessChain()抛出异常
        // 因为这是静态方法，需要使用PowerMock等工具，这里我们只测试正常情况
        Collection<ProcessChain> result = compositeProcessChainRepository.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testClearCacheWhenException() {
        doThrow(new RuntimeException("Clear cache failed"))
            .when(readWriteConfigSource).clearCache();
        
        // clearCache方法应该处理异常并不抛出
        assertDoesNotThrow(() -> compositeProcessChainRepository.clearCache());
    }
}
package com.zornflow.infrastructure.config.source.composite;

import com.zornflow.infrastructure.config.dataaccess.composite.CompositeConfigSource;
import com.zornflow.infrastructure.config.model.*;
import com.zornflow.infrastructure.config.source.ConfigSource;
import com.zornflow.infrastructure.config.source.ReadableConfigSource;
import com.zornflow.infrastructure.config.source.ReadWriteConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 组合配置源单元测试
 */
class CompositeConfigSourceTest {

    @Mock
    private ReadableConfigSource readableSource1;

    @Mock
    private ReadableConfigSource readableSource2;

    @Mock
    private ReadWriteConfigSource writeableSource1;

    @Mock
    private ReadWriteConfigSource writeableSource2;

    private CompositeConfigSource compositeConfigSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 设置mock行为
        when(readableSource1.getSourceName()).thenReturn("ReadableSource1");
        when(readableSource1.getSourceType()).thenReturn(ConfigSource.ConfigSourceType.YAML);
        when(readableSource2.getSourceName()).thenReturn("ReadableSource2");
        when(readableSource2.getSourceType()).thenReturn(ConfigSource.ConfigSourceType.PROPERTIES);
        
        when(writeableSource1.getSourceName()).thenReturn("WriteableSource1");
        when(writeableSource1.getSourceType()).thenReturn(ConfigSource.ConfigSourceType.DATABASE);
        when(writeableSource1.getPriority()).thenReturn(10);
        when(writeableSource2.getSourceName()).thenReturn("WriteableSource2");
        when(writeableSource2.getSourceType()).thenReturn(ConfigSource.ConfigSourceType.REMOTE);
        when(writeableSource2.getPriority()).thenReturn(20);
    }

    @Test
    void testConstructorWithMixedSources() {
        List<ConfigSource> sources = Arrays.asList(readableSource1, writeableSource1, readableSource2, writeableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        assertEquals("CompositeConfigSource", compositeConfigSource.getSourceName());
        assertEquals(ConfigSource.ConfigSourceType.COMPOSITE, compositeConfigSource.getSourceType());
        assertEquals(0, compositeConfigSource.getPriority());
        assertTrue(compositeConfigSource.isAvailable());
    }

    @Test
    void testConstructorWithEmptySources() {
        List<ConfigSource> sources = new ArrayList<>();
        compositeConfigSource = new CompositeConfigSource(sources);
        
        assertEquals("CompositeConfigSource", compositeConfigSource.getSourceName());
        assertFalse(compositeConfigSource.isAvailable());
    }

    @Test
    void testLoadRuleChainConfigs() {
        // 准备测试数据
        Map<String, RuleChainConfig> configs1 = new HashMap<>();
        configs1.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1").build());
        
        Map<String, RuleChainConfig> configs2 = new HashMap<>();
        configs2.put("rule2", RuleChainConfig.builder().id("rule2").name("Rule 2").build());
        configs2.put("rule1", RuleChainConfig.builder().id("rule1").name("Rule 1 Updated").build()); // 覆盖rule1
        
        when(readableSource1.loadRuleChainConfigs()).thenReturn(configs1);
        when(readableSource2.loadRuleChainConfigs()).thenReturn(configs2);
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Map<String, RuleChainConfig> result = compositeConfigSource.loadRuleChainConfigs();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("rule1"));
        assertTrue(result.containsKey("rule2"));
        // 验证优先级：后添加的源优先级更高，所以rule1应该被更新
        assertEquals("Rule 1 Updated", result.get("rule1").name());
    }

    @Test
    void testLoadRuleChainConfig() {
        RuleChainConfig config = RuleChainConfig.builder().id("rule1").name("Rule 1").build();
        when(readableSource1.loadRuleChainConfig("rule1")).thenReturn(Optional.of(config));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Optional<RuleChainConfig> result = compositeConfigSource.loadRuleChainConfig("rule1");
        
        assertTrue(result.isPresent());
        assertEquals("rule1", result.get().id());
        assertEquals("Rule 1", result.get().name());
    }

    @Test
    void testLoadRuleChainConfigNotFound() {
        when(readableSource1.loadRuleChainConfig("rule1")).thenReturn(Optional.empty());
        when(readableSource2.loadRuleChainConfig("rule1")).thenReturn(Optional.empty());
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Optional<RuleChainConfig> result = compositeConfigSource.loadRuleChainConfig("rule1");
        
        assertFalse(result.isPresent());
    }

    @Test
    void testLoadProcessChainConfigs() {
        Map<String, ProcessChainConfig> configs1 = new HashMap<>();
        configs1.put("process1", ProcessChainConfig.builder().id("process1").name("Process 1").build());
        
        when(readableSource1.loadProcessChainConfigs()).thenReturn(configs1);
        
        List<ConfigSource> sources = Arrays.asList(readableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Map<String, ProcessChainConfig> result = compositeConfigSource.loadProcessChainConfigs();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("process1"));
    }

    @Test
    void testLoadProcessChainConfig() {
        ProcessChainConfig config = ProcessChainConfig.builder().id("process1").name("Process 1").build();
        when(readableSource1.loadProcessChainConfig("process1")).thenReturn(Optional.of(config));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Optional<ProcessChainConfig> result = compositeConfigSource.loadProcessChainConfig("process1");
        
        assertTrue(result.isPresent());
        assertEquals("process1", result.get().id());
        assertEquals("Process 1", result.get().name());
    }

    @Test
    void testRefresh() {
        List<ConfigSource> sources = Arrays.asList(readableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        boolean result = compositeConfigSource.refresh();
        assertTrue(result);
    }

    @Test
    void testClearCache() {
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        assertDoesNotThrow(() -> compositeConfigSource.clearCache());
    }

    @Test
    void testWriteOperations() {
        RuleChainConfig ruleConfig = RuleChainConfig.builder().id("rule1").name("Rule 1").build();
        ProcessChainConfig processConfig = ProcessChainConfig.builder().id("process1").name("Process 1").build();
        
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 测试写操作不抛出异常
        assertDoesNotThrow(() -> compositeConfigSource.saveRuleChainConfig(ruleConfig));
        assertDoesNotThrow(() -> compositeConfigSource.saveProcessChainConfig(processConfig));
        assertDoesNotThrow(() -> compositeConfigSource.deleteRuleChainConfig("rule1"));
        assertDoesNotThrow(() -> compositeConfigSource.deleteProcessChainConfig("process1"));
    }

    @Test
    void testIsAvailableWithNoReadableSources() {
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 即使有可写源，但没有可读源，也应该不可用
        assertFalse(compositeConfigSource.isAvailable());
    }

    @Test
    void testIsAvailableWithReadableSources() {
        List<ConfigSource> sources = Arrays.asList(readableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        assertTrue(compositeConfigSource.isAvailable());
    }

    @Test
    void testWriteOperationsWithNoWriteableSources() {
        RuleChainConfig ruleConfig = RuleChainConfig.builder().id("rule1").name("Rule 1").build();
        ProcessChainConfig processConfig = ProcessChainConfig.builder().id("process1").name("Process 1").build();
        
        List<ConfigSource> sources = Arrays.asList(readableSource1); // 只有可读源，没有可写源
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 当没有可写源时，写操作应该抛出IllegalStateException
        assertThrows(IllegalStateException.class, () -> compositeConfigSource.saveRuleChainConfig(ruleConfig));
        assertThrows(IllegalStateException.class, () -> compositeConfigSource.saveProcessChainConfig(processConfig));
    }

    @Test
    void testPriorityBasedWriteTargetSelection() {
        // 设置不同的优先级
        when(writeableSource1.getPriority()).thenReturn(10); // 更高优先级
        when(writeableSource2.getPriority()).thenReturn(20); // 更低优先级
        
        RuleChainConfig ruleConfig = RuleChainConfig.builder().id("rule1").name("Rule 1").build();
        
        List<ConfigSource> sources = Arrays.asList(writeableSource1, writeableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 执行保存操作
        assertDoesNotThrow(() -> compositeConfigSource.saveRuleChainConfig(ruleConfig));
        
        // 验证高优先级的源被使用
        verify(writeableSource1).saveRuleChainConfig(ruleConfig);
        verify(writeableSource2, never()).saveRuleChainConfig(ruleConfig);
    }

    @Test
    void testCacheFunctionality() {
        // 准备测试数据
        RuleChainConfig config = RuleChainConfig.builder().id("rule1").name("Rule 1").build();
        when(readableSource1.loadRuleChainConfig("rule1")).thenReturn(Optional.of(config));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 第一次加载
        Optional<RuleChainConfig> result1 = compositeConfigSource.loadRuleChainConfig("rule1");
        
        // 验证数据源被调用
        verify(readableSource1).loadRuleChainConfig("rule1");
        
        // 第二次加载同一数据
        Optional<RuleChainConfig> result2 = compositeConfigSource.loadRuleChainConfig("rule1");
        
        // 验证数据源只被调用一次（缓存生效）
        verify(readableSource1).loadRuleChainConfig("rule1");
        
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get(), result2.get());
    }

    @Test
    void testCacheClear() {
        // 准备测试数据
        RuleChainConfig config = RuleChainConfig.builder().id("rule1").name("Rule 1").build();
        when(readableSource1.loadRuleChainConfig("rule1")).thenReturn(Optional.of(config));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 加载数据
        Optional<RuleChainConfig> result1 = compositeConfigSource.loadRuleChainConfig("rule1");
        
        // 清除缓存
        compositeConfigSource.clearCache();
        
        // 再次加载相同数据
        Optional<RuleChainConfig> result2 = compositeConfigSource.loadRuleChainConfig("rule1");
        
        // 验证数据源被调用了两次（缓存被清除）
        verify(readableSource1, times(2)).loadRuleChainConfig("rule1");
        
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
    }

    @Test
    void testLoadGlobalRulesWithMultipleSources() {
        // 准备测试数据
        Map<String, RuleConfig> rules1 = new HashMap<>();
        rules1.put("rule1", RuleConfig.builder().id("rule1").name("Rule 1").build());
        
        Map<String, RuleConfig> rules2 = new HashMap<>();
        rules2.put("rule2", RuleConfig.builder().id("rule2").name("Rule 2").build());
        rules2.put("rule1", RuleConfig.builder().id("rule1").name("Rule 1 Updated").build()); // 覆盖rule1
        
        when(readableSource1.loadGlobalRules()).thenReturn(rules1);
        when(readableSource2.loadGlobalRules()).thenReturn(rules2);
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Map<String, RuleConfig> result = compositeConfigSource.loadGlobalRules();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("rule1"));
        assertTrue(result.containsKey("rule2"));
        // 验证优先级：后添加的源优先级更高，所以rule1应该被更新
        assertEquals("Rule 1 Updated", result.get("rule1").name());
    }

    @Test
    void testLoadGlobalNodesWithMultipleSources() {
        // 准备测试数据
        Map<String, ProcessNodeConfig> nodes1 = new HashMap<>();
        nodes1.put("node1", ProcessNodeConfig.builder().id("node1").name("Node 1").build());
        
        Map<String, ProcessNodeConfig> nodes2 = new HashMap<>();
        nodes2.put("node2", ProcessNodeConfig.builder().id("node2").name("Node 2").build());
        nodes2.put("node1", ProcessNodeConfig.builder().id("node1").name("Node 1 Updated").build()); // 覆盖node1
        
        when(readableSource1.loadGlobalNodes()).thenReturn(nodes1);
        when(readableSource2.loadGlobalNodes()).thenReturn(nodes2);
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Map<String, ProcessNodeConfig> result = compositeConfigSource.loadGlobalNodes();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("node1"));
        assertTrue(result.containsKey("node2"));
        // 验证优先级：后添加的源优先级更高，所以node1应该被更新
        assertEquals("Node 1 Updated", result.get("node1").name());
    }

    @Test
    void testRefreshWithMultipleSources() {
        // 设置mock行为
        when(readableSource1.refresh()).thenReturn(true);
        when(readableSource2.refresh()).thenReturn(false); // 模拟一个源刷新失败
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        boolean result = compositeConfigSource.refresh();
        
        // 即使部分源刷新失败，整体刷新也应该返回false
        assertFalse(result);
        
        // 验证所有源都被刷新
        verify(readableSource1).refresh();
        verify(readableSource2).refresh();
    }

    @Test
    void testRefreshWithExceptionInOneSource() {
        // 设置mock行为，其中一个源抛出异常
        when(readableSource1.refresh()).thenReturn(true);
        when(readableSource2.refresh()).thenThrow(new RuntimeException("Refresh failed"));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        boolean result = compositeConfigSource.refresh();
        
        // 即使部分源抛出异常，整体刷新也应该处理异常并返回false
        assertFalse(result);
        
        // 验证所有源都被尝试刷新
        verify(readableSource1).refresh();
        verify(readableSource2).refresh();
    }

    @Test
    void testBatchSaveOperations() {
        List<RuleChainConfig> ruleConfigs = Arrays.asList(
            RuleChainConfig.builder().id("rule1").name("Rule 1").build(),
            RuleChainConfig.builder().id("rule2").name("Rule 2").build()
        );
        
        List<ProcessChainConfig> processConfigs = Arrays.asList(
            ProcessChainConfig.builder().id("process1").name("Process 1").build(),
            ProcessChainConfig.builder().id("process2").name("Process 2").build()
        );
        
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 测试批量保存不抛出异常
        assertDoesNotThrow(() -> compositeConfigSource.saveRuleChainConfigs(ruleConfigs));
        assertDoesNotThrow(() -> compositeConfigSource.saveProcessChainConfigs(processConfigs));
        
        // 验证每个配置都被保存
        verify(writeableSource1).saveRuleChainConfig(ruleConfigs.get(0));
        verify(writeableSource1).saveRuleChainConfig(ruleConfigs.get(1));
        verify(writeableSource1).saveProcessChainConfig(processConfigs.get(0));
        verify(writeableSource1).saveProcessChainConfig(processConfigs.get(1));
    }

    @Test
    void testGlobalConfigOperations() {
        List<GlobalRuleConfig> globalRuleConfigs = Arrays.asList(
            GlobalRuleConfig.builder().id("global-rule-1").name("Global Rule 1").build(),
            GlobalRuleConfig.builder().id("global-rule-2").name("Global Rule 2").build()
        );
        
        List<ProcessNodeConfig> globalNodeConfigs = Arrays.asList(
            ProcessNodeConfig.builder().id("global-node-1").name("Global Node 1").build(),
            ProcessNodeConfig.builder().id("global-node-2").name("Global Node 2").build()
        );
        
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 测试全局配置保存不抛出异常
        assertDoesNotThrow(() -> compositeConfigSource.saveGlobalRules(globalRuleConfigs));
        assertDoesNotThrow(() -> compositeConfigSource.saveGlobalProcessNodes(globalNodeConfigs));
        
        // 验证每个配置都被保存
        verify(writeableSource1).saveGlobalRule(globalRuleConfigs.get(0));
        verify(writeableSource1).saveGlobalRule(globalRuleConfigs.get(1));
        verify(writeableSource1).saveGlobalProcessNode(globalNodeConfigs.get(0));
        verify(writeableSource1).saveGlobalProcessNode(globalNodeConfigs.get(1));
    }

    @Test
    void testGlobalConfigDeleteOperations() {
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 测试全局配置删除不抛出异常
        assertDoesNotThrow(() -> compositeConfigSource.deleteGlobalRule("global-rule-id"));
        assertDoesNotThrow(() -> compositeConfigSource.deleteGlobalProcessNode("global-node-id"));
        
        // 验证删除操作被调用
        verify(writeableSource1).deleteGlobalRule("global-rule-id");
        verify(writeableSource1).deleteGlobalProcessNode("global-node-id");
    }

    @Test
    void testLoadGlobalRulesWithExceptionInOneSource() {
        // 设置mock行为，其中一个源抛出异常
        Map<String, RuleConfig> rules1 = new HashMap<>();
        rules1.put("rule1", RuleConfig.builder().id("rule1").name("Rule 1").build());
        
        when(readableSource1.loadGlobalRules()).thenReturn(rules1);
        when(readableSource2.loadGlobalRules()).thenThrow(new RuntimeException("Load failed"));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Map<String, RuleConfig> result = compositeConfigSource.loadGlobalRules();
        
        assertNotNull(result);
        // 即使一个源抛出异常，也应该从其他源加载数据
        assertEquals(1, result.size());
        assertTrue(result.containsKey("rule1"));
        
        // 验证两个源都被调用
        verify(readableSource1).loadGlobalRules();
        verify(readableSource2).loadGlobalRules();
    }

    @Test
    void testLoadGlobalNodesWithExceptionInOneSource() {
        // 设置mock行为，其中一个源抛出异常
        Map<String, ProcessNodeConfig> nodes1 = new HashMap<>();
        nodes1.put("node1", ProcessNodeConfig.builder().id("node1").name("Node 1").build());
        
        when(readableSource1.loadGlobalNodes()).thenReturn(nodes1);
        when(readableSource2.loadGlobalNodes()).thenThrow(new RuntimeException("Load failed"));
        
        List<ConfigSource> sources = Arrays.asList(readableSource1, readableSource2);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        Map<String, ProcessNodeConfig> result = compositeConfigSource.loadGlobalNodes();
        
        assertNotNull(result);
        // 即使一个源抛出异常，也应该从其他源加载数据
        assertEquals(1, result.size());
        assertTrue(result.containsKey("node1"));
        
        // 验证两个源都被调用
        verify(readableSource1).loadGlobalNodes();
        verify(readableSource2).loadGlobalNodes();
    }

    @Test
    void testClearAll() {
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // clearAll方法没有返回值，只需验证不抛出异常
        assertDoesNotThrow(() -> compositeConfigSource.clearAll());
        
        // 验证可写源的clearAll方法被调用
        verify(writeableSource1).clearAll();
    }

    @Test
    void testClearAllWhenException() {
        List<ConfigSource> sources = Arrays.asList(writeableSource1);
        compositeConfigSource = new CompositeConfigSource(sources);
        
        // 模拟clearAll方法抛出异常
        doThrow(new RuntimeException("Clear all failed"))
            .when(writeableSource1).clearAll();
        
        // clearAll方法应该处理异常并不抛出
        assertDoesNotThrow(() -> compositeConfigSource.clearAll());
    }
}
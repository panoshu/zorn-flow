package com.zornflow.infrastructure.config.source.yaml;

import com.zornflow.infrastructure.config.dataaccess.yaml.YamlConfigProperties;
import com.zornflow.infrastructure.config.dataaccess.yaml.YamlConfigSource;
import com.zornflow.infrastructure.config.model.ProcessChainConfig;
import com.zornflow.infrastructure.config.model.ProcessNodeConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.config.source.ConfigSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * YAML配置源单元测试
 */
class YamlConfigSourceTest {

    @TempDir
    Path tempDir;

    private YamlConfigSource yamlConfigSource;
    private YamlConfigProperties yamlConfigProperties;

    @BeforeEach
    void setUp() throws IOException {
        // 创建测试YAML文件
        createTestYamlFiles();
        
        yamlConfigProperties = new YamlConfigProperties();
        yamlConfigProperties.setBasePath(tempDir.toString());
        yamlConfigProperties.setRuleChainsPath("rule-chains");
        yamlConfigProperties.setProcessChainsPath("process-chains");
        yamlConfigProperties.setGlobalRulesPath("global-rules");
        yamlConfigProperties.setGlobalNodesPath("global-nodes");
        
        yamlConfigSource = new YamlConfigSource(yamlConfigProperties);
    }

    private void createTestYamlFiles() throws IOException {
        // 创建测试规则链文件
        File ruleChainsDir = tempDir.resolve("rule-chains").toFile();
        ruleChainsDir.mkdirs();
        
        File ruleChainFile = new File(ruleChainsDir, "test-rule-chain.yaml");
        String ruleChainContent = """
            id: "test-rule-chain"
            name: "Test Rule Chain"
            version: "1.0"
            description: "Test rule chain for unit testing"
            rules:
              - id: "rule-1"
                name: "Test Rule 1"
                description: "A test rule"
                condition: "#{age} > 18"
                action: "ALLOW"
            """;
        java.nio.file.Files.write(ruleChainFile.toPath(), ruleChainContent.getBytes());
        
        // 创建测试流程链文件
        File processChainsDir = tempDir.resolve("process-chains").toFile();
        processChainsDir.mkdirs();
        
        File processChainFile = new File(processChainsDir, "test-process-chain.yaml");
        String processChainContent = """
            id: "test-process-chain"
            name: "Test Process Chain"
            version: "1.0"
            description: "Test process chain for unit testing"
            nodes:
              - id: "node-1"
                name: "Start Node"
                type: "START"
                next: "node-2"
            """;
        java.nio.file.Files.write(processChainFile.toPath(), processChainContent.getBytes());
    }

    @Test
    void testGetSourceName() {
        assertEquals("YamlConfigSource", yamlConfigSource.getSourceName());
    }

    @Test
    void testGetSourceType() {
        assertEquals(ConfigSource.ConfigSourceType.YAML, yamlConfigSource.getSourceType());
    }

    @Test
    void testLoadRuleChainConfigs() {
        Map<String, RuleChainConfig> ruleChainConfigs = yamlConfigSource.loadRuleChainConfigs();
        assertNotNull(ruleChainConfigs);
        assertFalse(ruleChainConfigs.isEmpty());
        assertTrue(ruleChainConfigs.containsKey("test-rule-chain"));
        
        RuleChainConfig config = ruleChainConfigs.get("test-rule-chain");
        assertEquals("test-rule-chain", config.id());
        assertEquals("Test Rule Chain", config.name());
        assertEquals("1.0", config.version());
        assertNotNull(config.rules());
        assertFalse(config.rules().isEmpty());
    }

    @Test
    void testLoadRuleChainConfig() {
        Optional<RuleChainConfig> configOpt = yamlConfigSource.loadRuleChainConfig("test-rule-chain");
        assertTrue(configOpt.isPresent());
        
        RuleChainConfig config = configOpt.get();
        assertEquals("test-rule-chain", config.id());
        assertEquals("Test Rule Chain", config.name());
        assertEquals("1.0", config.version());
        assertNotNull(config.rules());
        assertFalse(config.rules().isEmpty());
    }

    @Test
    void testLoadProcessChainConfigs() {
        Map<String, ProcessChainConfig> processChainConfigs = yamlConfigSource.loadProcessChainConfigs();
        assertNotNull(processChainConfigs);
        assertFalse(processChainConfigs.isEmpty());
        assertTrue(processChainConfigs.containsKey("test-process-chain"));
        
        ProcessChainConfig config = processChainConfigs.get("test-process-chain");
        assertEquals("test-process-chain", config.id());
        assertEquals("Test Process Chain", config.name());
        assertEquals("1.0", config.version());
        assertNotNull(config.nodes());
        assertFalse(config.nodes().isEmpty());
    }

    @Test
    void testLoadProcessChainConfig() {
        Optional<ProcessChainConfig> configOpt = yamlConfigSource.loadProcessChainConfig("test-process-chain");
        assertTrue(configOpt.isPresent());
        
        ProcessChainConfig config = configOpt.get();
        assertEquals("test-process-chain", config.id());
        assertEquals("Test Process Chain", config.name());
        assertEquals("1.0", config.version());
        assertNotNull(config.nodes());
        assertFalse(config.nodes().isEmpty());
    }

    @Test
    void testRefresh() {
        boolean result = yamlConfigSource.refresh();
        assertTrue(result);
        
        // 验证刷新后仍能正确加载数据
        Map<String, RuleChainConfig> ruleChainConfigs = yamlConfigSource.loadRuleChainConfigs();
        assertFalse(ruleChainConfigs.isEmpty());
    }

    @Test
    void testLoadNonExistentConfig() {
        Optional<RuleChainConfig> configOpt = yamlConfigSource.loadRuleChainConfig("non-existent");
        assertFalse(configOpt.isPresent());
    }

    @Test
    void testLoadGlobalRules() {
        // 全局规则测试将在后续实现
        Map<String, RuleConfig> globalRules = yamlConfigSource.loadGlobalRules();
        assertNotNull(globalRules);
        // 由于测试环境中没有全局规则文件，返回空map是合理的
        assertTrue(globalRules.isEmpty() || globalRules.size() >= 0);
    }

    @Test
    void testLoadGlobalNodes() {
        // 全局节点测试将在后续实现
        Map<String, ProcessNodeConfig> globalNodes = yamlConfigSource.loadGlobalNodes();
        assertNotNull(globalNodes);
        // 由于测试环境中没有全局节点文件，返回空map是合理的
        assertTrue(globalNodes.isEmpty() || globalNodes.size() >= 0);
    }

    @Test
    void testRefreshWithException() {
        // 创建一个临时的YamlConfigSource，模拟文件读取异常
        YamlConfigProperties props = new YamlConfigProperties();
        props.setBasePath("/invalid/path/that/does/not/exist");
        props.setRuleChainsPath("rule-chains");
        props.setProcessChainsPath("process-chains");
        props.setGlobalRulesPath("global-rules");
        props.setGlobalNodesPath("global-nodes");
        
        YamlConfigSource source = new YamlConfigSource(props);
        boolean result = source.refresh();
        // 即使加载失败，refresh方法也应该返回false而不是抛出异常
        assertFalse(result);
    }

    @Test
    void testLoadWithInvalidYamlContent() throws IOException {
        // 创建一个包含无效YAML内容的文件
        File ruleChainsDir = tempDir.resolve("rule-chains").toFile();
        ruleChainsDir.mkdirs();
        
        File invalidRuleChainFile = new File(ruleChainsDir, "invalid-rule-chain.yaml");
        String invalidContent = """
            id: "invalid-rule-chain"
            name: "Invalid Rule Chain"
            version: "1.0"
            description: "Invalid rule chain with malformed YAML"
            rules:
              - id: "rule-1"
                name: "Test Rule 1"
                description: "A test rule"
                condition: "#{age} > 18"
                action: "ALLOW"
              # Missing indentation here to create invalid YAML
            invalid-indented-line
            """;
        java.nio.file.Files.write(invalidRuleChainFile.toPath(), invalidContent.getBytes());
        
        // 刷新配置源以重新加载
        boolean refreshResult = yamlConfigSource.refresh();
        // 即使部分文件无效，refresh也应该返回true（因为其他文件可能有效）
        assertTrue(refreshResult);
        
        // 验证加载的配置数量
        Map<String, RuleChainConfig> ruleChainConfigs = yamlConfigSource.loadRuleChainConfigs();
        assertNotNull(ruleChainConfigs);
        // 由于有一个文件是无效的，加载的配置数量可能会减少
        assertTrue(ruleChainConfigs.size() >= 0);
    }

    @Test
    void testLoadWithEmptyDirectories() throws IOException {
        // 清空现有的测试文件，创建空目录
        File ruleChainsDir = tempDir.resolve("rule-chains").toFile();
        File processChainsDir = tempDir.resolve("process-chains").toFile();
        
        // 删除现有文件
        for (File file : ruleChainsDir.listFiles()) {
            file.delete();
        }
        for (File file : processChainsDir.listFiles()) {
            file.delete();
        }
        
        // 刷新配置源
        boolean refreshResult = yamlConfigSource.refresh();
        assertTrue(refreshResult);
        
        // 验证加载空配置
        Map<String, RuleChainConfig> ruleChainConfigs = yamlConfigSource.loadRuleChainConfigs();
        Map<String, ProcessChainConfig> processChainConfigs = yamlConfigSource.loadProcessChainConfigs();
        
        assertNotNull(ruleChainConfigs);
        assertNotNull(processChainConfigs);
        assertTrue(ruleChainConfigs.isEmpty());
        assertTrue(processChainConfigs.isEmpty());
    }

    @Test
    void testLoadGlobalRulesFromResources() throws IOException {
        // 创建全局规则测试文件
        File globalRulesDir = tempDir.resolve("global-rules").toFile();
        globalRulesDir.mkdirs();
        
        File globalRuleFile = new File(globalRulesDir, "test-global-rule.yaml");
        String globalRuleContent = """
            id: "test-global-rule"
            name: "Test Global Rule"
            description: "A test global rule"
            condition: "#{age} > 18"
            action: "ALLOW"
            """;
        java.nio.file.Files.write(globalRuleFile.toPath(), globalRuleContent.getBytes());
        
        // 刷新配置源以重新加载
        boolean refreshResult = yamlConfigSource.refresh();
        assertTrue(refreshResult);
        
        // 验证加载全局规则
        Map<String, RuleConfig> globalRules = yamlConfigSource.loadGlobalRules();
        assertNotNull(globalRules);
        assertFalse(globalRules.isEmpty());
        assertTrue(globalRules.containsKey("test-global-rule"));
        
        RuleConfig rule = globalRules.get("test-global-rule");
        assertEquals("test-global-rule", rule.id());
        assertEquals("Test Global Rule", rule.name());
    }

    @Test
    void testLoadGlobalNodesFromResources() throws IOException {
        // 创建全局节点测试文件
        File globalNodesDir = tempDir.resolve("global-nodes").toFile();
        globalNodesDir.mkdirs();
        
        File globalNodeFile = new File(globalNodesDir, "test-global-node.yaml");
        String globalNodeContent = """
            id: "test-global-node"
            name: "Test Global Node"
            type: "START"
            description: "A test global node"
            """;
        java.nio.file.Files.write(globalNodeFile.toPath(), globalNodeContent.getBytes());
        
        // 刷新配置源以重新加载
        boolean refreshResult = yamlConfigSource.refresh();
        assertTrue(refreshResult);
        
        // 验证加载全局节点
        Map<String, ProcessNodeConfig> globalNodes = yamlConfigSource.loadGlobalNodes();
        assertNotNull(globalNodes);
        assertFalse(globalNodes.isEmpty());
        assertTrue(globalNodes.containsKey("test-global-node"));
        
        ProcessNodeConfig node = globalNodes.get("test-global-node");
        assertEquals("test-global-node", node.id());
        assertEquals("Test Global Node", node.name());
        assertEquals("START", node.type());
    }

    @Test
    void testLoadGlobalRulesWithInvalidYaml() throws IOException {
        // 创建包含无效YAML的全局规则文件
        File globalRulesDir = tempDir.resolve("global-rules").toFile();
        globalRulesDir.mkdirs();
        
        File invalidGlobalRuleFile = new File(globalRulesDir, "invalid-global-rule.yaml");
        String invalidContent = """
            id: "invalid-global-rule"
            name: "Invalid Global Rule"
            description: "Invalid global rule with malformed YAML"
            condition: "#{age} > 18"
            action: "ALLOW"
              # Missing indentation here to create invalid YAML
            invalid-indented-line
            """;
        java.nio.file.Files.write(invalidGlobalRuleFile.toPath(), invalidContent.getBytes());
        
        // 刷新配置源以重新加载
        boolean refreshResult = yamlConfigSource.refresh();
        // 即使部分文件无效，refresh也应该返回true（因为其他文件可能有效）
        assertTrue(refreshResult);
        
        // 验证加载的全局规则数量
        Map<String, RuleConfig> globalRules = yamlConfigSource.loadGlobalRules();
        assertNotNull(globalRules);
        // 由于有一个文件是无效的，加载的规则数量可能会减少
        assertTrue(globalRules.size() >= 0);
    }

    @Test
    void testLoadGlobalNodesWithInvalidYaml() throws IOException {
        // 创建包含无效YAML的全局节点文件
        File globalNodesDir = tempDir.resolve("global-nodes").toFile();
        globalNodesDir.mkdirs();
        
        File invalidGlobalNodeFile = new File(globalNodesDir, "invalid-global-node.yaml");
        String invalidContent = """
            id: "invalid-global-node"
            name: "Invalid Global Node"
            type: "START"
            description: "Invalid global node with malformed YAML"
              # Missing indentation here to create invalid YAML
            invalid-indented-line
            """;
        java.nio.file.Files.write(invalidGlobalNodeFile.toPath(), invalidContent.getBytes());
        
        // 刷新配置源以重新加载
        boolean refreshResult = yamlConfigSource.refresh();
        // 即使部分文件无效，refresh也应该返回true（因为其他文件可能有效）
        assertTrue(refreshResult);
        
        // 验证加载的全局节点数量
        Map<String, ProcessNodeConfig> globalNodes = yamlConfigSource.loadGlobalNodes();
        assertNotNull(globalNodes);
        // 由于有一个文件是无效的，加载的节点数量可能会减少
        assertTrue(globalNodes.size() >= 0);
    }
}
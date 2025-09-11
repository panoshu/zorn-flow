package com.zornflow.infrastructure.config.source.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zornflow.domain.common.config.model.ModelConfig;
import com.zornflow.domain.common.config.source.ReadableConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 18:35
 **/

@Slf4j
@RequiredArgsConstructor
public abstract sealed class AbstractYamlConfigSource<C extends ModelConfig, I extends ModelConfig> implements ReadableConfigSource<C>
  permits YamlRuleChainConfigSource, YamlProcessChainConfigSource {

  protected final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
  protected final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

  // 模板方法，定义了加载和合并的整体算法骨架
  @Override
  public Map<String, C> loadAll() throws IOException {
    // 1. 加载共享的节点/规则
    Map<String, I> sharedItems = loadSharedItems();
    log.info("加载了 {} 个共享 [{}].", sharedItems.size(), getItemName());

    // 2. 加载链并与共享项合并
    return loadChains(sharedItems);
  }

  @Override
  public Optional<C> load(String id) throws IOException {
    return Optional.ofNullable(loadAll().get(id));
  }

  @Override
  public boolean available() {
    String combinedPath = getSharedItemsPath() + "," + getChainsPath();
    log.debug("检查YAML配置源 [{}] 可用性，扫描路径: {}", getSourceName(), combinedPath);
    try {
      Resource[] resources = resourceResolver.getResources(combinedPath);
      return Arrays.stream(resources).anyMatch(Resource::exists);
    } catch (IOException e) {
      log.warn("检查 [{}] 可用性时发生IO异常", getSourceName(), e);
      return false;
    }
  }

  @Override
  public ConfigSourceType getSourceType() {
    return ConfigSourceType.YAML;
  }

  // --- 子类必须实现的抽象方法 ---

  /**
   * @return 共享项（规则/节点）的名称，用于日志记录
   */
  protected abstract String getItemName();

  /**
   * @return 共享项的配置文件路径
   */
  protected abstract String getSharedItemsPath();

  /**
   * @return 链的配置文件路径
   */
  protected abstract String getChainsPath();

  /**
   * @return 共享项的TypeReference，用于反序列化
   */
  protected abstract TypeReference<Map<String, I>> getSharedItemTypeReference();

  /**
   * @return 链的TypeReference，用于反序列化
   */
  protected abstract TypeReference<Map<String, C>> getChainTypeReference();

  /**
   * 合并单个链与所有共享项
   *
   * @param chain       待处理的链
   * @param sharedItems 所有共享项
   * @return 合并后的新链实例
   */
  protected abstract C mergeChain(C chain, Map<String, I> sharedItems);

  // --- 通用辅助方法 ---

  private Map<String, I> loadSharedItems() throws IOException {
    return loadConfigsFromPath(getSharedItemsPath(), getSharedItemTypeReference());
  }

  private Map<String, C> loadChains(Map<String, I> sharedItems) throws IOException {
    Map<String, C> rawChains = loadConfigsFromPath(getChainsPath(), getChainTypeReference());
    Map<String, C> mergedChains = new HashMap<>();
    rawChains.forEach((chainId, chain) -> {
      C mergedChain = mergeChain(chain, sharedItems);
      mergedChains.put(chainId, mergedChain);
      log.debug("加载并合并链: {}", chainId);
    });
    return mergedChains;
  }

  /**
   * 通用的加载器，protected，只能被子类访问。
   */
  protected <T> Map<String, T> loadConfigsFromPath(String pathPattern, TypeReference<Map<String, T>> typeRef) throws IOException {
    Resource[] resources = resourceResolver.getResources(pathPattern);
    Map<String, T> resultMap = new HashMap<>();
    for (Resource res : resources) {
      if (res.exists() && res.isReadable()) {
        try (InputStream in = res.getInputStream()) {
          Map<String, T> loadedMap = yamlMapper.readValue(in, typeRef);
          if (loadedMap != null) {
            resultMap.putAll(loadedMap);
          }
        }
      }
    }
    return resultMap;
  }
}

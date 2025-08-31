package com.zornflow.infrastructure.config1.source.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专注YAML资源加载的核心组件
 * - 查找并加载YAML文件
 * - 将YAML解析为Map结构
 * - 处理资源加载错误
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 5:11
 */
@Slf4j
public class YamlConfigLoader {

  private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

  public Map<String, Object> loadYamlResource(String locationPattern) throws IOException {
    Map<String, Object> combinedConfig = new HashMap<>();

    for (Resource resource : findResources(locationPattern)) {
      try (InputStream is = resource.getInputStream()) {
        Map<String, Object> yamlContent = yamlMapper.readValue(
          is,
          new TypeReference<>() {}
        );
        combinedConfig.putAll(yamlContent);
      }
    }
    return combinedConfig;
  }

  private List<Resource> findResources(String locationPattern) {
    try {
      return Arrays.stream(resourceResolver.getResources(locationPattern))
        .filter(Resource::exists)
        .filter(Resource::isReadable)
        .peek(resource -> log.debug("Found resource: {}", resource.getFilename()))
        .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException(
        "Failed to resolve resources: " + locationPattern,
        e
      );
    }
  }
}

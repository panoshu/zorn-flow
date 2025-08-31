package com.zornflow.infrastructure.config1.source.yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 4:57
 */
@Slf4j
public final class YamlParser {
  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

  public static <T> Map<String, T> load(Resource[] resources, Function<Map<String,Object>,T> converter) {
    Map<String, T> result = new HashMap<>();
    for (Resource r : resources) {
      if (!r.exists()) continue;
      try (InputStream in = r.getInputStream()) {
        Map<String, Object> yaml = MAPPER.readValue(in, new TypeReference<>() {});
        yaml.forEach((k, v) -> result.put(k, converter.apply((Map<String, Object>) v)));
      } catch (IOException e) {
        log.warn("skip resource {}", r.getFilename(), e);
      }
    }
    return result;
  }
}

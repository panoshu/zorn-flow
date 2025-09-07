package com.zornflow.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/31 20:24
 **/

@Component
@RequiredArgsConstructor
public class JsonbMapperHelper {

  // ObjectMapper 实例从Spring容器注入，以保证配置一致
  private final ObjectMapper objectMapper;

  /**
   * 将 JSONB 数据转换为指定类型的 Java 对象。
   *
   * @param jsonb         从数据库读取的 JSONB 对象
   * @param typeReference 目标对象的类型引用，用于处理泛型
   * @param <T>           目标类型
   * @return 转换后的 Java 对象，如果输入为 null 则返回 null
   */
  public <T> T fromJsonb(JSONB jsonb, TypeReference<T> typeReference) {
    if (jsonb == null || jsonb.data() == null) {
      return null;
    }
    try {
      return objectMapper.readValue(jsonb.data(), typeReference);
    } catch (JsonProcessingException e) {
      // 在实际应用中，可以替换为更具体的业务异常
      throw new RuntimeException("无法将JSONB解析为对象", e);
    }
  }

  /**
   * 将 Java 对象序列化为 JSONB 类型以便存入数据库。
   *
   * @param object 要序列化的 Java 对象
   * @return 转换后的 JSONB 对象，如果输入为 null 则返回 null
   */
  public JSONB toJsonb(Object object) {
    if (object == null) {
      return null;
    }
    try {
      return JSONB.valueOf(objectMapper.writeValueAsString(object));
    } catch (JsonProcessingException e) {
      // 在实际应用中，可以替换为更具体的业务异常
      throw new RuntimeException("无法将对象序列化为JSONB", e);
    }
  }
}

package com.zornflow.infrastructure.presistence.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zornflow.domain.common.service.BusinessContextSerializer;
import com.zornflow.domain.common.valueobject.BusinessContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/30 15:30
 **/
@Service
public class BusinessContextJacksonSerializer implements BusinessContextSerializer {

  private static final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule());

  @Override
  public String serialize(BusinessContext context) {
    try {
      return objectMapper.writeValueAsString(context.data());
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize BusinessContext", e);
    }
  }

  @Override
  public BusinessContext deserialize(String json) {
    try {
      TypeReference<Map<String, Object>> typeRef =
        new TypeReference<Map<String, Object>>() {
        };
      Map<String, Object> data = objectMapper.readValue(json, typeRef);
      return new BusinessContext(data);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid JSON format for BusinessContext", e);
    }
  }
}

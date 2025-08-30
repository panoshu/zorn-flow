package com.zornflow.domain.common.valueobject;

import com.domain.contract.valueobject.BaseValueObject;
import com.zornflow.domain.common.service.BusinessContextSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/25 9:15
 */
public record BusinessContext(Map<String, Object> data) implements BaseValueObject {
  public BusinessContext {
    // 使用ConcurrentHashMap确保在多线程环境下（如并行网关）的安全性
    data = new ConcurrentHashMap<>(data != null ? data : Map.of());
  }

  public <T> T get(String key, Class<T> type) {
    return type.cast(data.get(key));
  }

  public BusinessContext with(String key, Object value) {
    var newData = new ConcurrentHashMap<>(this.data);
    newData.put(key, value);
    return new BusinessContext(newData);
  }

  public BusinessContext merge(Map<String, Object> otherData) {
    if (otherData == null || otherData.isEmpty()) {
      return this;
    }
    var newData = new ConcurrentHashMap<>(this.data);
    newData.putAll(otherData);
    return new BusinessContext(newData);
  }

  public String toJson(BusinessContextSerializer serializer) {
    return serializer.serialize(this);
  }

  public static BusinessContext fromJson(String json, BusinessContextSerializer deserializer) {
    return deserializer.deserialize(json);
  }
}

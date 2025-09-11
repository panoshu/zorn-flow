package com.zornflow.domain.common.types.identifier;

import com.domain.contract.valueobject.EntityId;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/10 22:09
 **/

public final class DomainIds {
  private static final Map<Class<? extends EntityId>, IdStrategy<?>> POOL = new ConcurrentHashMap<>();

  public static void register(Map<Class<? extends EntityId>, IdStrategy<?>> map) {
    POOL.putAll(map);
  }
  @SuppressWarnings("unchecked")
  private static <T> IdStrategy<T> strategyOf(Class<?> idType) {
    IdStrategy<?> s = POOL.get(idType);
    if (s == null) throw new IllegalStateException("No strategy for " + idType);
    return (IdStrategy<T>) s;
  }

  /** 生成新ID（含非空+格式校验） */
  public static <T extends EntityId> T next(Class<T> idClass, Function<String, T> constructor) {
    IdStrategy<String> st = strategyOf(idClass);
    String raw = Objects.requireNonNull(st.generate(), "Generated value null");

    if (!st.validator().test(raw))
      throw new IllegalArgumentException("Generated ID [ %s ] invalid".formatted(raw));

    return constructor.apply(raw);
  }

  /** 外部字符复原ID（含格式校验） */
  public static <T extends EntityId> T of(String raw, Class<T> idClass, Function<String, T> constructor) {
    Objects.requireNonNull(raw, "Input ID value cannot be null");
    IdStrategy<Object> st = strategyOf(idClass);

    if (!st.validator().test(raw))
      throw new IllegalArgumentException("Invalid [ %s ] [ %s ]".formatted(idClass.getSimpleName(), raw));

    return constructor.apply(raw);
  }

}

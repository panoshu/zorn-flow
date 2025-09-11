package com.zornflow.infrastructure.adapter.identifier;

import com.domain.contract.valueobject.EntityId;
import com.zornflow.domain.common.types.identifier.DomainIds;
import com.zornflow.domain.common.types.identifier.IdAlgorithm;
import com.zornflow.domain.common.types.identifier.IdStrategy;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/10 22:25
 **/

@Component
public class IdAlgorithmBinder implements SmartInitializingSingleton {

  private final Map<String, IdStrategy<?>> algoMap;
  private final IdStrategy<?> defaultStrategy;

  public IdAlgorithmBinder(List<IdStrategy<?>> strategyBeans) {
    // ① 收集策略（构造函数阶段已完成注入）
    this.algoMap = strategyBeans.stream()
      .collect(Collectors.toMap(
        s -> s.getClass().getAnnotation(IdAlgorithm.class).value(),
        Function.identity(),
        (a, b) -> a));
    // ② 选默认（构造函数阶段已完成注入）
    this.defaultStrategy = algoMap.values().stream()
      .filter(s -> s.getClass().getAnnotation(IdAlgorithm.class).isDefault())
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("缺少默认算法"));
  }

  @Override
  public void afterSingletonsInstantiated() {
    // ③ 扫描ID类型（仍需反射，放这里）
    Set<Class<? extends EntityId>> idTypes = scanIdTypes();
    // ④ 构建绑定表
    Map<Class<? extends EntityId>, IdStrategy<?>> binding = buildBindingMap(idTypes);
    // ⑤ 注入静态门面
    DomainIds.register(binding);
  }

  private Set<Class<? extends EntityId>> scanIdTypes() {
    return new Reflections(new ConfigurationBuilder().forPackages("domain"))
      .getSubTypesOf(EntityId.class);
  }

  private Map<Class<? extends EntityId>, IdStrategy<?>> buildBindingMap(Set<Class<? extends EntityId>> idTypes) {
    Map<Class<? extends EntityId>, IdStrategy<?>> map = new HashMap<>();

    for (Class<? extends EntityId> idType : idTypes) {
      IdAlgorithm algo = idType.getAnnotation(IdAlgorithm.class);
      String algoName = (algo == null || algo.value().isEmpty())
        ? idType.getSimpleName().toLowerCase(Locale.ROOT).replace("id", "")
        : algo.value();
      if ("manual".equals(algoName)) continue;
      map.put(idType, algoMap.getOrDefault(algoName, defaultStrategy));
    }
    return map;
  }
}

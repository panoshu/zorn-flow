package com.zornflow.infrastructure.adapter.identifier;

import com.domain.contract.valueobject.EntityId;
import com.zornflow.domain.common.types.identifier.DomainIds;
import com.zornflow.domain.common.types.identifier.IdAlgorithm;
import com.zornflow.domain.common.types.identifier.IdStrategy;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;

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

@Configuration
public class IdAutoConfig {

  public IdAutoConfig(List<IdStrategy<?>> strategies) {
    // 1. 算法名 -> 策略
    Map<String, IdStrategy<?>> algoMap = strategies.stream()
      .collect(Collectors.toMap(
        s -> s.getClass().getAnnotation(IdAlgorithm.class).value(),
        Function.identity(),
        (a, b) -> a));

    // 2. 兜底策略
    IdStrategy<?> defaultStrategy = algoMap.values().stream()
      .filter(s -> s.getClass().getAnnotation(IdAlgorithm.class).isDefault())
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("缺少默认算法"));

    // 3. 扫描所有 EntityId 子类型（Reflections 0.10+ 正确用法）
    Reflections reflections = new Reflections(
      new ConfigurationBuilder()
        .forPackages("domain")   // 你的根包
        .filterInputsBy(s -> s.endsWith("Id.class")) // 可选优化
    );
    Set<Class<? extends EntityId>> idTypes = reflections.getSubTypesOf(EntityId.class);

    // 4. 绑定映射
    Map<Class<?>, IdStrategy<?>> binding = new HashMap<>();
    for (Class<? extends EntityId> idType : idTypes) {
      IdAlgorithm algo = idType.getAnnotation(IdAlgorithm.class);
      String algoName = (algo == null || algo.value().isEmpty())
        ? idType.getSimpleName()
        .toLowerCase(Locale.ROOT)
        .replace("id", "")
        : algo.value();
      IdStrategy<?> strategy = algoMap.getOrDefault(algoName, defaultStrategy);
      binding.put(idType, strategy);
    }
    DomainIds.register(binding);
  }
}

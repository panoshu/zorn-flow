package com.zornflow.infrastructure.adapter.identifier;

import com.zornflow.domain.common.types.identifier.IdAlgorithm;
import com.zornflow.domain.common.types.identifier.IdStrategy;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/10 22:24
 **/

@IdAlgorithm(value = "uuid", isDefault = true)
@Component
public class UuidStrategy implements IdStrategy<String> {
  @Override
  public String generate() {
    return UUID.randomUUID().toString();
  }

  @Override
  public Predicate<String> validator() {
    return s -> true;
  }
}

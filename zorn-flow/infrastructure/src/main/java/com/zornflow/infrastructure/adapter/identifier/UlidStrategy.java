package com.zornflow.infrastructure.adapter.identifier;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zornflow.domain.common.types.identifier.IdAlgorithm;
import com.zornflow.domain.common.types.identifier.IdStrategy;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/10 22:21
 **/

@IdAlgorithm(value = "ulid", isDefault = true)
@Component
public class UlidStrategy implements IdStrategy<String> {
  @Override
  public String generate() {
    return UlidCreator.getUlid().toString();
  }

  @Override
  public Predicate<String> validator() {
    return Ulid::isValid;
  }
}

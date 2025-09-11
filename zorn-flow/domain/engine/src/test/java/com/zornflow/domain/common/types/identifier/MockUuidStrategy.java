package com.zornflow.domain.common.types.identifier;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/11 20:11
 **/

public class MockUuidStrategy implements IdStrategy<String> {
  @Override
  public String generate() {
    return "uuid-" + UUID.randomUUID();
  }

  @Override
  public Predicate<String> validator() {
    return s -> s.startsWith("uuid-");
  }
}

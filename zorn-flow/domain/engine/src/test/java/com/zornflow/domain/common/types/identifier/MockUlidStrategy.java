package com.zornflow.domain.common.types.identifier;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/11 20:11
 **/

public class MockUlidStrategy implements IdStrategy<String> {
  private static final AtomicLong COUNTER = new AtomicLong(0);

  @Override
  public String generate() {
    return "01TEST" + String.format("%020d", COUNTER.getAndIncrement());
  }

  @Override
  public Predicate<String> validator() {
    return s -> s.length() == 26 && s.startsWith("01TEST");
  }
}

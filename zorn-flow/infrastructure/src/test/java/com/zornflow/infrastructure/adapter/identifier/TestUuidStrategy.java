package com.zornflow.infrastructure.adapter.identifier;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/11 20:08
 **/

import com.zornflow.domain.common.types.identifier.IdStrategy;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * 内存版 UUID 策略
 */
public class TestUuidStrategy implements IdStrategy<String> {
  @Override
  public String generate() {
    return UUID.randomUUID().toString();
  }

  @Override
  public Predicate<String> validator() {
    return s -> s != null && s.matches("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
  }
}

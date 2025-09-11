package com.zornflow.infrastructure.adapter.identifier;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/11 20:08
 **/

import com.zornflow.domain.common.types.identifier.IdStrategy;

import java.util.function.Predicate;

/**
 * 内存版 ULID-look-alike 策略：26位固定字符 + 自增，足够测试排序 & 格式校验
 */
public class TestUlidStrategy implements IdStrategy<String> {
  private static final String PREFIX = "01TEST";
  private static int counter = 0;

  @Override
  public String generate() {
    return PREFIX + String.format("%020d", counter++);
  }

  @Override
  public Predicate<String> validator() {
    return s -> s != null && s.length() == 26 && s.startsWith(PREFIX);
  }
}

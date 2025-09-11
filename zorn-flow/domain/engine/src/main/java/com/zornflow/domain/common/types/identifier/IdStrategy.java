package com.zornflow.domain.common.types.identifier;

import java.util.function.Predicate;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/10 22:09
 **/

public interface IdStrategy<T> {
  T generate();

  Predicate<T> validator();
}

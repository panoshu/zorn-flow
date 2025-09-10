package com.zornflow.domain.common.types.identifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/10 22:10
 **/

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdAlgorithm {
  /**
   * 算法名，大小写不敏感
   * 写在策略类上 = "我能提供什么算法"
   * 写在ID类上   = "我想要什么算法"
   */
  String value();

  /** 是否兜底算法，只能有一个true */
  boolean isDefault() default false;
}

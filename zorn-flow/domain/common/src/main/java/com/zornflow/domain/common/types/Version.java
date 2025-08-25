package com.zornflow.domain.common.types;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 15:57
 */
public record Version(String value) {
  private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

  public Version {
    Objects.requireNonNull(value, "版本号不能为空");
    if (!VERSION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("版本号格式不正确，必须符合x.y.z格式");
    }
  }

  /**
   * 创建版本号实例
   * @param value 版本号字符串
   * @return 版本号对象
   */
  public static Version of(String value) {
    return new Version(value);
  }

  /**
   * 创建默认版本号(1.0.0)
   * @return 默认版本号对象
   */
  public static Version defaultVersion() {
    return new Version("1.0.0");
  }

  /**
   * 比较当前版本是否高于另一个版本
   * @param other 其他版本
   * @return true表示当前版本更高
   */
  public boolean isHigherThan(Version other) {
    String[] thisParts = this.value.split("\\.");
    String[] otherParts = other.value.split("\\.");
    int length = Math.max(thisParts.length, otherParts.length);

    for (int i = 0; i < length; i++) {
      int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
      int otherPart = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;

      if (thisPart > otherPart) return true;
      if (thisPart < otherPart) return false;
    }
    return false;
  }

  public boolean isEqual(Version other) {
    return !isHigherThan(other) && !other.isHigherThan(this);
  }

  public boolean isLowerThan(Version other) {
    return other.isHigherThan(this);
  }
}

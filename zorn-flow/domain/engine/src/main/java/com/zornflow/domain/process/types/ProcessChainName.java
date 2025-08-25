package com.zornflow.domain.process.types;

import com.ddd.contract.valueobject.DomainPrimitive;
import com.ddd.contract.valueobject.Identifier;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 22:12
 **/

public record ProcessChainName(String value) implements DomainPrimitive, Identifier {
  public ProcessChainName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("流程链ID不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("流程链ID长度不能超过40个字符");
    }
  }

  public static ProcessChainName of(String value) {
    return new ProcessChainName(value);
  }

  public static ProcessChainName of(ProcessChainId processChainId) {
    return new ProcessChainName(processChainId.value());
  }
}

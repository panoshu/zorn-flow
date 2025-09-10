package com.zornflow.domain.process.types;

import com.domain.contract.valueobject.DomainPrimitive;
import com.domain.contract.valueobject.EntityId;
import com.zornflow.domain.common.types.identifier.DomainIds;

/**
 * 流程 ID 领域原语
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/31 16:13
 */
public record ProcessChainId(String value) implements EntityId {
  public ProcessChainId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("流程链ID不能为空");
    }
    if (value.length() > 40) {
      throw new IllegalArgumentException("流程链ID长度不能超过40个字符");
    }
  }

  public static ProcessChainId generate() {
    return DomainIds.next(ProcessChainId.class, ProcessChainId::new);
  }
  public static ProcessChainId of(String raw) {
    return DomainIds.of(raw, ProcessChainId.class, ProcessChainId::new);
  }

}

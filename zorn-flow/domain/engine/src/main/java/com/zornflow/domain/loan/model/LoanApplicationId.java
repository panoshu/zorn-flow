package com.zornflow.domain.loan.model;

import com.domain.contract.valueobject.EntityId;
import com.zornflow.domain.common.types.identifier.DomainIds;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/8 03:11
 **/

// 业务ID，可以使用ULID或UUID
public record LoanApplicationId(String value) implements EntityId {
  public static LoanApplicationId generate() {
    return DomainIds.next(LoanApplicationId.class, LoanApplicationId::new);
  }

  public static LoanApplicationId of(String raw) {
    return DomainIds.of(raw, LoanApplicationId.class, LoanApplicationId::new);
  }
}

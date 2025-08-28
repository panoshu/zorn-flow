package com.domain.contract.valueobject;

/**
 * DDD 值对象 (Value Object) 根契约
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 11:15
 */
public sealed interface ValueObject
  permits BaseValueObject, DomainPrimitive{
}

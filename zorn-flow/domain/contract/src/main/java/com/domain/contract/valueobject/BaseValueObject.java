package com.domain.contract.valueobject;

/**
 * 可供外部实现的、非密封的值对象基础接口
 *
 * <p>
 * 任何希望成为领域值对象的类, 都应实现这个接口
 * 强烈建议使用 Record 来实现，以确保其不变性和结构相等性
 * </p>
 * <p>
 * <b>使用示例: </b>
 * <br/>
 * {@code public record Address(String street, Long zipcode) implements BaseValueObject {}}
 * </p>
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 11:34
 */
public non-sealed interface BaseValueObject extends ValueObject {
}

package com.ddd.contract.identifier;

/**
 * 可供外部实现的、非密封的标识符基础接口
 *
 * <p>
 * 任何希望成为领域标识符的类, 都应实现这个接口
 * 强烈建议使用 Record 来实现，以获得开箱即用的不变性和值相等性
 * </p>
 * <p>
 * <b>使用示例: </b>
 * <br/>
 * {@code public record XxxxId(String value) implements Identifier {}}
 * <br/>
 * {@code public record XxxxId(Long value) implements Identifier {}}
 * </p>
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/24 11:29
 */
public non-sealed interface BaseIdentifier extends Identifier{
}

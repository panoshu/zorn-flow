package matcher;

/**
 * 可匹配对象接口。
 * 任何可以被 MatcherService 匹配的领域对象都应实现此接口。
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 12:34
 */

public interface Matchable {
  Matcher getMatcher();
}

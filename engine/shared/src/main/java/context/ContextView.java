package context;

/**
 * 上下文视图接口。
 * 为 MatcherService 提供一个统一的、只读的业务数据访问方式。
 * 具体的 BusinessContext 将实现此接口。
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 12:36
 */

public interface ContextView {
  /**
   * 根据点状导航路径获取属性值。
   * 例如 "customer.address.city"
   * @param key 属性路径
   * @return 属性值，如果不存在则返回 null
   */
  Object getProperty(String key);
}

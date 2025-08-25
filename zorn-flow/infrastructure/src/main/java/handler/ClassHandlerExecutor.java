package handler;

import com.zornflow.domain.common.types.BusinessContext;
import com.zornflow.domain.rule.service.HandlerExecutor;
import com.zornflow.domain.rule.valueobject.HandlerConfig;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 23:35
 **/

@Component
@AllArgsConstructor
public class ClassHandlerExecutor implements HandlerExecutor {

  private final ApplicationContext applicationContext; // 用于从Spring容器中获取Bean

  // 定义一个业务模块需要实现的接口，以规范化调用
  public interface RuleExecutable {
    void execute(BusinessContext context);
  }

  private static final String HANDLER_TYPE = "class";

  @Override
  public boolean supports(HandlerConfig handler) {
    return HANDLER_TYPE.equalsIgnoreCase(handler.type().value());
  }

  @Override
  public void execute(HandlerConfig handler, BusinessContext context) {
    try {
      // handler.identifier() 应该是Spring Bean的名称
      String beanName = handler.getClass().getSimpleName();
      Object bean = applicationContext.getBean(beanName);

      if (bean instanceof RuleExecutable executable) {
        // 如果Bean实现了我们定义的标准接口，直接调用
        executable.execute(context);
      } else {
        // 也可以支持通过反射调用任意方法，但约定接口更健壮
        throw new UnsupportedOperationException("Bean " + beanName + " does not implement RuleExecutable interface.");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to execute class handler: " + handler.getClass().getSimpleName(), e);
    }
  }
}

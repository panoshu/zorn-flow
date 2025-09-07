package com.zornflow.infrastructure.adapter.handler;

import com.zornflow.domain.common.valueobject.BusinessContext;
import com.zornflow.domain.rule.service.HandlerExecutor;
import com.zornflow.domain.rule.types.HandlerType;
import com.zornflow.domain.rule.valueobject.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 23:35
 **/

@Service
@RequiredArgsConstructor
public class ClassHandlerExecutor implements HandlerExecutor {

  private final ApplicationContext applicationContext; // 用于从Spring容器中获取Bean

  @Override
  public boolean supports(Handler handler) {
    return HandlerType.CLASS.equals(handler.type());
  }

  @Override
  public void execute(Handler handler, BusinessContext context) {
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

  // 定义一个业务模块需要实现的接口，以规范化调用
  public interface RuleExecutable {
    void execute(BusinessContext context);
  }
}

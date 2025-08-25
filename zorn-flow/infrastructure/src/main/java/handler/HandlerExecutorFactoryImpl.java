package handler;

import com.zornflow.domain.rule.service.HandlerExecutor;
import com.zornflow.domain.rule.service.HandlerExecutorFactory;
import com.zornflow.domain.rule.valueobject.HandlerConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/25 23:33
 **/

@Component
public class HandlerExecutorFactoryImpl implements HandlerExecutorFactory {

  private final List<HandlerExecutor> executors;

  /**
   * Spring会自动将容器中所有IHandlerExecutor的实现类注入到这个List中。
   *
   * @param executors 所有IHandlerExecutor的Bean实例
   */
  public HandlerExecutorFactoryImpl(List<HandlerExecutor> executors) {
    this.executors = executors;
  }

  @Override
  public Optional<HandlerExecutor> getExecutor(HandlerConfig handler) {
    return executors.stream()
      .filter(executor -> executor.supports(handler))
      .findFirst();
  }
}

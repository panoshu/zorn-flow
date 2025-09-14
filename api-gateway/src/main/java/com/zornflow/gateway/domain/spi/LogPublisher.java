package com.zornflow.gateway.domain.spi;

import com.zornflow.gateway.infrastructure.model.RequestLog;
import com.zornflow.gateway.infrastructure.model.ResponseLog;
import reactor.core.publisher.Mono;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 15:32
 **/

public interface LogPublisher {
  Mono<Void> publishRequest(RequestLog requestLog);
  Mono<Void> publishResponse(ResponseLog responseLog);
}

package com.zornflow.gateway;

import com.zornflow.gateway.infrastructure.properties.CryptoProperties;
import com.zornflow.gateway.infrastructure.properties.GlobalSecurityProperties;
import com.zornflow.gateway.infrastructure.properties.LogProperties;
import com.zornflow.gateway.infrastructure.properties.ReplayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:49
 **/

@SpringBootApplication
@EnableConfigurationProperties({
  GlobalSecurityProperties.class,
  CryptoProperties.class,
  ReplayProperties.class,
  LogProperties.class
})
@EnableCaching
public class ApiGateway {

  public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
  }
}

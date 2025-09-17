package com.zornflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 13:49
 **/

@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGateway {

  public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
  }
}

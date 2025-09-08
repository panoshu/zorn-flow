package com.zornflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/26 22:23
 **/

@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties
public class ZornflowApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZornflowApplication.class, args);
  }
}

package com.zornflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/26 22:23
 **/

@SpringBootApplication
@EnableConfigurationProperties
public class ZornflowApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZornflowApplication.class, args);
  }
}

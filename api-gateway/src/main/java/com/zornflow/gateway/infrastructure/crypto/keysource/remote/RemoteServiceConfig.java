package com.zornflow.gateway.infrastructure.crypto.keysource.remote;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:11
 **/

@ConfigurationProperties(prefix = "security.crypto.remote-service")
record RemoteServiceConfig(String url) {}

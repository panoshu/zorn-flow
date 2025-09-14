package com.zornflow.gateway.infrastructure.crypto.keysource;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/9/14 14:11
 **/

@ConfigurationProperties(prefix = "gateway.security.crypto.remote-service")
public record RemoteCryptoConfig(String url) {}

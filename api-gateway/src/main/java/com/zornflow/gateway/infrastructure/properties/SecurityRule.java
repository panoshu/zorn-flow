package com.zornflow.gateway.infrastructure.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/9/16 17:22
 */

@Component
@RefreshScope
@RequiredArgsConstructor
public class SecurityRule {

  private final GlobalProperties globalProperties;
  private final ReplayProperties replayProperties;
  private final LoggingProperties loggingProperties;
  private final CryptoProperties cryptoProperties;
  private final SecurityRuleParser securityRuleParser = new SecurityRuleParser();

  public static SecurityRule of(
    GlobalProperties globalProperties,
    ReplayProperties replayProperties,
    LoggingProperties loggingProperties,
    CryptoProperties cryptoProperties
  ){
    return new SecurityRule(globalProperties, replayProperties, loggingProperties, cryptoProperties);
  }

  public boolean isGlobalSecurityDisable(ServerWebExchange exchange) {
    return !this.isGlobalSecurityEnabled(exchange);
  }

  public boolean isGlobalSecurityEnabled(ServerWebExchange exchange) {
    return securityRuleParser.isApplicable(
      globalProperties.enabled(),
      globalProperties.excludePaths(),
      exchange
    );
  }

  public boolean shouldSkipCrypto(ServerWebExchange exchange){
    return !this.shouldApplyCrypto(exchange);
  }

  public boolean shouldApplyCrypto(ServerWebExchange exchange){
    return this.isGlobalSecurityEnabled(exchange) && securityRuleParser.isApplicable(
      cryptoProperties.enabled(),
      cryptoProperties.excludePaths(),
      exchange
    );
  }

  public boolean shouldSkipReplayProtection(ServerWebExchange exchange){
    return !this.shouldApplyReplayProtection(exchange);
  }

  public boolean shouldApplyReplayProtection(ServerWebExchange exchange) {
    return this.isGlobalSecurityEnabled(exchange) && securityRuleParser.isApplicable(
      replayProperties.enabled(),
      replayProperties.excludePaths(),
      exchange
    );
  }

  public boolean shouldSkipLogging(ServerWebExchange exchange) {
    return !this.shouldApplyLogging(exchange);
  }

  public boolean shouldApplyLogging(ServerWebExchange exchange) {
    return this.isGlobalSecurityEnabled(exchange) && securityRuleParser.isApplicable(
      loggingProperties.enabled(),
      loggingProperties.excludePaths(),
      exchange
    );
  }

  public long maxBodySize(){
    return cryptoProperties.maxBodySize().toBytes();
  }

  public CryptoProperties.EncryptFailureStrategy onEncryptFailure(){
    return cryptoProperties.onEncryptFailure();
  }
}

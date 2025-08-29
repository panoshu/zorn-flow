package com.zornflow.infrastructure.config;

import com.zornflow.infrastructure.config.model.RuleConfig;
import com.zornflow.infrastructure.config.model.RuleChainConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * description
 *
 * @author <a href="mailto: me@panoshu.top">panoshu</a>
 * @version 1.0
 * @since 2025/8/28 22:15
 **/
@ExtendWith(MockitoExtension.class)
class ClasspathConfigLoaderTest {

  private static ClasspathConfigLoader loader;

  @BeforeAll
  static void setup() {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    ConfigLocationProperties props = new ConfigLocationProperties();
    loader = new ClasspathConfigLoader(resolver, props);
  }

  /* ===== 正案例 ===== */
  @Test
  void load_ok() {
    Optional<RuleConfig> ruleOpt = loader.rule("biz-rule1");
    assertThat(ruleOpt).isPresent();
    RuleConfig r = ruleOpt.get();
    assertThat(r.id()).isEqualTo("biz-rule1");
    assertThat(r.name()).isEqualTo("共享规则1");
    assertThat(r.priority()).isEqualTo(100);
    assertThat(r.handle().type().toString()).isEqualTo("CLASS");

    Optional<RuleChainConfig> rcOpt = loader.ruleChain("loan-risk-rules");
    assertThat(rcOpt).isPresent();
    List<RuleConfig> ruleConfigs = rcOpt.get().rules();
    assertThat(ruleConfigs).hasSize(3);
    assertThat(ruleConfigs.get(0).priority()).isEqualTo(10);
    assertThat(ruleConfigs.get(2).priority()).isEqualTo(110);
  }

}

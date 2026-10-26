/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest(properties = "proxigaz.admin.bootstrap.enabled=false")
@ActiveProfiles("data_test")
class AdminUserBootstrapDisabledDataTest {

  @Autowired private ApplicationContext applicationContext;

  @Test
  void disabledBootstrap_doesNotRegisterStartupRunner() {
    assertThat(applicationContext.getBeansOfType(ApplicationRunner.class))
        .doesNotContainKey("adminUserBootstrapRunner");
  }
}

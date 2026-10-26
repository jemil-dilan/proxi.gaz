/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.reference;

import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ReferenceBeans {

  @Bean
  ReferenceGenerator referenceGenerator(JdbcTemplate jdbcTemplate) {
    return new ReferenceGeneratorJdbc(jdbcTemplate);
  }
}

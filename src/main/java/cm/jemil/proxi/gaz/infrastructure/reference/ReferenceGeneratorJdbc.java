package cm.jemil.proxi.gaz.infrastructure.reference;

import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
class ReferenceGeneratorJdbc implements ReferenceGenerator {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public String generateForDemo() {
    Long next = jdbcTemplate.queryForObject("SELECT nextval('seq_demo_reference')", Long.class);
    return "%03d".formatted(next);
  }
}

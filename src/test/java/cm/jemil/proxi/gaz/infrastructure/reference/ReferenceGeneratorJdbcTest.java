package cm.jemil.proxi.gaz.infrastructure.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class ReferenceGeneratorJdbcTest {

  private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
  private final ReferenceGeneratorJdbc referenceGenerator =
      new ReferenceGeneratorJdbc(jdbcTemplate);

  @Test
  void generateForDemo_shouldReturnZeroPaddedSequenceValue() {
    // Given
    when(jdbcTemplate.queryForObject(eq("SELECT nextval('seq_demo_reference')"), eq(Long.class)))
        .thenReturn(101L);

    // When
    String index = referenceGenerator.generateForDemo();

    // Then
    assertThat(index).isEqualTo("101");
  }

  @Test
  void generateForDemo_shouldPadToThreeDigits() {
    // Given
    when(jdbcTemplate.queryForObject(eq("SELECT nextval('seq_demo_reference')"), eq(Long.class)))
        .thenReturn(7L);

    // When
    String index = referenceGenerator.generateForDemo();

    // Then
    assertThat(index).isEqualTo("007");
  }
}

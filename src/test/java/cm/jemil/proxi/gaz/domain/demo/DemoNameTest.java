package cm.jemil.proxi.gaz.domain.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DemoNameTest {

  @Test
  void from_shouldCreateDemoName() {
    // Given
    String name = "test";

    // When
    DemoName demoName = DemoName.from(name);

    // Then
    assertThat(demoName).isNotNull();
    assertThat(demoName.value()).isEqualTo(name);
  }
}

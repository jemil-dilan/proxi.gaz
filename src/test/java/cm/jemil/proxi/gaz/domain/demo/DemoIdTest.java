/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DemoIdTest {

  @Test
  void from_shouldCreateDemoId() {
    // Given
    UUID id = UUID.randomUUID();

    // When
    DemoId demoId = new DemoId(id);

    // Then
    assertThat(demoId).isNotNull();
    assertThat(demoId.value()).isEqualTo(id);
  }

  @Test
  void generate_shouldCreateUniqueIds() {
    // When
    DemoId first = DemoId.generate();
    DemoId second = DemoId.generate();

    // Then
    assertThat(first.value()).isNotEqualTo(second.value());
  }
}

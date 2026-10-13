package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoReference;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JpaMapperTest {

  private final JpaMapper jpaMapper = new JpaMapperImpl();

  private static final UUID DEMO_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  @Test
  void fromDomain_shouldMapDemoToDemoJpa() {
    // Given
    Demo demo =
        Demo.of(
            DemoId.from(DEMO_ID),
            DemoName.from("Demo"),
            DemoReference.fromIndex("001"),
            DemoDescription.from("Description"));

    // When
    DemoJpa demoJpa = jpaMapper.fromDomain(demo);

    // Then
    assertThat(demoJpa.getId()).isEqualTo(DEMO_ID);
    assertThat(demoJpa.getName()).isEqualTo("Demo");
    assertThat(demoJpa.getReference()).isEqualTo("DEMO-001");
    assertThat(demoJpa.getDescription()).isEqualTo("Description");
    assertThat(demoJpa.isDeleted()).isFalse();
  }

  @Test
  void toDomain_shouldMapDemoJpaToDemo() {
    // Given
    DemoJpa demoJpa = new DemoJpa();
    demoJpa.setId(DEMO_ID);
    demoJpa.setName("Demo");
    demoJpa.setReference("DEMO-001");
    demoJpa.setDescription("Description");

    // When
    Demo demo = jpaMapper.toDomain(demoJpa);

    // Then
    assertThat(demo.getId()).isEqualTo(DemoId.from(DEMO_ID));
    assertThat(demo.getName()).isEqualTo(DemoName.from("Demo"));
    assertThat(demo.getReference()).isEqualTo(DemoReference.fromIndex("001"));
    assertThat(demo.getDescription()).isEqualTo(DemoDescription.from("Description"));
  }

  @Test
  void roundTrip_shouldPreserveDemoState() {
    // Given
    Demo demo =
        Demo.of(
            DemoId.from(DEMO_ID),
            DemoName.from("Demo"),
            DemoReference.fromIndex("042"),
            DemoDescription.from("Description"));

    // When
    Demo restored = jpaMapper.toDomain(jpaMapper.fromDomain(demo));

    // Then
    assertThat(restored.getId()).isEqualTo(demo.getId());
    assertThat(restored.getName()).isEqualTo(demo.getName());
    assertThat(restored.getReference()).isEqualTo(demo.getReference());
    assertThat(restored.getDescription()).isEqualTo(demo.getDescription());
  }
}

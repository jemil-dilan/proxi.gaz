/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DemoTest {

  private static final UUID ID_VALUE = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  private static final DemoId ID = DemoId.from(ID_VALUE);
  private static final DemoName NAME = DemoName.from("Initial Demo");
  private static final DemoName NEW_NAME = DemoName.from("Updated Demo");
  private static final DemoReference REFERENCE = DemoReference.fromIndex("001");
  private static final DemoDescription DESCRIPTION = DemoDescription.from("Initial description");
  private static final DemoDescription NEW_DESCRIPTION =
      DemoDescription.from("Updated description");

  @Test
  void of_shouldBuildDemoWithAllFields() {
    // When
    Demo demo = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);

    // Then
    assertThat(demo).isNotNull();
    assertThat(demo.getId()).isEqualTo(ID);
    assertThat(demo.getName()).isEqualTo(NAME);
    assertThat(demo.getReference()).isEqualTo(REFERENCE);
    assertThat(demo.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  void of_shouldExposeSameIdThroughIdAccessor() {
    // Given
    Demo demo = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);

    // When / Then
    assertThat(demo.id()).isEqualTo(ID_VALUE);
    assertThat(demo.id()).isEqualTo(ID.value());
  }

  @Test
  void reconstitute_shouldBuildDemoWithAllFields() {
    // When
    Demo demo = Demo.reconstitute(ID, NAME, REFERENCE, DESCRIPTION);

    // Then
    assertThat(demo).isNotNull();
    assertThat(demo.getId()).isEqualTo(ID);
    assertThat(demo.getName()).isEqualTo(NAME);
    assertThat(demo.getReference()).isEqualTo(REFERENCE);
    assertThat(demo.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  void reconstitute_shouldPreserveReferenceForReadOnlyScenarios() {
    // Given
    DemoReference customReference = DemoReference.fromIndex("042");

    // When
    Demo demo = Demo.reconstitute(ID, NAME, customReference, DESCRIPTION);

    // Then
    assertThat(demo.getReference()).isEqualTo(customReference);
    assertThat(demo.getReference().value()).isEqualTo("DEMO-042");
  }

  @Test
  void update_shouldMutateBothNameAndDescriptionOnAggregate() {
    // Given
    Demo demo = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);

    // When
    demo.update(NEW_NAME, NEW_DESCRIPTION);

    // Then
    assertThat(demo.getName()).isEqualTo(NEW_NAME);
    assertThat(demo.getDescription()).isEqualTo(NEW_DESCRIPTION);
  }

  @Test
  void update_shouldPreserveIdAndReferenceOnAggregate() {
    // Given - immutable fields (id, reference) must NOT change across updates
    Demo demo = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);

    // When
    demo.update(NEW_NAME, NEW_DESCRIPTION);

    // Then
    assertThat(demo.getId()).isEqualTo(ID);
    assertThat(demo.id()).isEqualTo(ID_VALUE);
    assertThat(demo.getReference()).isEqualTo(REFERENCE);
  }

  @Test
  void update_shouldReturnUpdatedEventCarryingNewValues() {
    // Given
    Demo demo = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);

    // When
    DemoUpdatedEvent event = demo.update(NEW_NAME, NEW_DESCRIPTION);

    // Then
    assertThat(event).isNotNull();
    assertThat(event.id()).isEqualTo(ID);
    assertThat(event.name()).isEqualTo(NEW_NAME);
    assertThat(event.description()).isEqualTo(NEW_DESCRIPTION);
  }

  @Test
  void toCreatedEvent_shouldCarryAggregateState() {
    // Given
    Demo demo = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);

    // When
    DemoCreatedEvent event = demo.toCreatedEvent();

    // Then
    assertThat(event.id()).isEqualTo(ID);
    assertThat(event.name()).isEqualTo(NAME);
    assertThat(event.reference()).isEqualTo(REFERENCE);
    assertThat(event.description()).isEqualTo(DESCRIPTION);
  }

  @Test
  void equals_shouldBeBasedOnIdOnly() {
    // Given
    Demo one = Demo.of(ID, NAME, REFERENCE, DESCRIPTION);
    Demo other = Demo.of(ID, NEW_NAME, REFERENCE, NEW_DESCRIPTION);
    Demo different = Demo.of(DemoId.generate(), NAME, REFERENCE, DESCRIPTION);

    // When / Then
    assertThat(one).isEqualTo(other);
    assertThat(one.hashCode()).isEqualTo(other.hashCode());
    assertThat(one).isNotEqualTo(different);
  }

  @Test
  void fromIndex_shouldFormatReferenceWithDemoPrefix() {
    assertThat(DemoReference.fromIndex("101").value()).isEqualTo("DEMO-101");
    assertThat(DemoReference.fromIndex("007").value()).isEqualTo("DEMO-007");
  }
}

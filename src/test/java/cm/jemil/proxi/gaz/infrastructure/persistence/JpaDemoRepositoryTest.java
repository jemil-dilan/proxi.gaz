package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import cm.jemil.proxi.gaz.domain.demo.DemoReference;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class JpaDemoRepositoryTest {

  private static final UUID SEEDED_ID =
      UUID.fromString("c03b8308-edb8-4800-aa7b-29cb9d5ee4a7");
  private static final UUID SEEDED_DELETED_ID =
      UUID.fromString("47929b74-9498-435e-be6b-6b10348676e8");

  @Autowired private DemoRepository demoRepository;

  @Test
  void load_shouldReturnSeededDemo() {
    // When
    Demo demo = demoRepository.load(DemoId.from(SEEDED_ID));

    // Then
    assertThat(demo.getName()).isEqualTo(DemoName.from("FetchDemo_Name1"));
    assertThat(demo.getReference()).isEqualTo(DemoReference.from("DEMO-001"));
    assertThat(demo.getDescription())
        .isEqualTo(DemoDescription.from("FetchDemo_Name1_Description"));
  }

  @Test
  void load_shouldThrowNotFound_forUnknownId() {
    // Given
    var unknownId = DemoId.from(UUID.randomUUID());

    // When / Then
    assertThatThrownBy(() -> demoRepository.load(unknownId))
        .isInstanceOf(DemoNotFoundException.class);
  }

  @Test
  void load_shouldThrowNotFound_forSoftDeletedDemo() {
    // Given
    var deletedId = DemoId.from(SEEDED_DELETED_ID);

    // When / Then
    assertThatThrownBy(() -> demoRepository.load(deletedId))
        .isInstanceOf(DemoNotFoundException.class);
  }

  @Test
  void loadAllAsView1_shouldExcludeSoftDeletedDemos() {
    // When
    var views = demoRepository.loadAllAsView1();

    // Then
    assertThat(views)
        .extracting(view -> view.getDemoId())
        .contains(SEEDED_ID)
        .doesNotContain(SEEDED_DELETED_ID);
  }

  @Test
  void loadAsView1_shouldReturnProjectedFields() {
    // When
    var view = demoRepository.loadAsView1(DemoId.from(SEEDED_ID));

    // Then
    assertThat(view.getDemoId()).isEqualTo(SEEDED_ID);
    assertThat(view.getName()).isEqualTo("FetchDemo_Name1");
    assertThat(view.getReference()).isEqualTo("DEMO-001");
  }

  @Test
  void loadAsView2_shouldReturnProjectedFieldsIncludingDescription() {
    // When
    var view = demoRepository.loadAsView2(DemoId.from(SEEDED_ID));

    // Then
    assertThat(view.getDemoId()).isEqualTo(SEEDED_ID);
    assertThat(view.getName()).isEqualTo("FetchDemo_Name1");
    assertThat(view.getReference()).isEqualTo("DEMO-001");
    assertThat(view.getDescription()).isEqualTo("FetchDemo_Name1_Description");
  }

  @Test
  void existsByName_shouldMatchOnlyNonDeletedDemos() {
    assertThat(demoRepository.existsByName(DemoName.from("FetchDemo_Name1"))).isTrue();
    assertThat(demoRepository.existsByName(DemoName.from("FetchDemo_Name3"))).isFalse();
    assertThat(demoRepository.existsByName(DemoName.from("Unknown"))).isFalse();
  }

  @Test
  void existsByNameAndIdNot_shouldExcludeTheGivenDemo() {
    // Given - the name of the seeded demo, evaluated for another demo
    var otherId = DemoId.from(UUID.randomUUID());

    // When / Then
    assertThat(demoRepository.existsByNameAndIdNot("FetchDemo_Name1", otherId)).isTrue();
    assertThat(demoRepository.existsByNameAndIdNot("FetchDemo_Name1", DemoId.from(SEEDED_ID)))
        .isFalse();
  }

  @Test
  void insert_thenLoad_shouldPersistAllFields() {
    // Given
    Demo demo = newDemo("Insert_RoundTrip");

    // When
    demoRepository.insert(demo);

    // Then
    Demo loaded = demoRepository.load(demo.getId());
    assertThat(loaded.getName()).isEqualTo(DemoName.from("Insert_RoundTrip"));
    assertThat(loaded.getReference()).isEqualTo(DemoReference.fromIndex("001"));
    assertThat(loaded.getDescription()).isEqualTo(DemoDescription.from("Insert_RoundTrip_Desc"));
    assertThat(demoRepository.existsById(demo.getId())).isTrue();
  }

  @Test
  void update_shouldPersistNewNameAndKeepReference() {
    // Given
    Demo demo = newDemo("Update_Before");
    demoRepository.insert(demo);

    // When
    demo.update(DemoName.from("Update_After"), DemoDescription.from("Update_After_Desc"));
    demoRepository.update(demo);

    // Then
    Demo loaded = demoRepository.load(demo.getId());
    assertThat(loaded.getName()).isEqualTo(DemoName.from("Update_After"));
    assertThat(loaded.getDescription()).isEqualTo(DemoDescription.from("Update_After_Desc"));
    assertThat(loaded.getReference()).isEqualTo(DemoReference.fromIndex("001"));
  }

  @Test
  void delete_shouldSoftDeleteTheDemo() {
    // Given
    Demo demo = newDemo("Delete_RoundTrip");
    demoRepository.insert(demo);
    var demoId = demo.getId();

    // When
    demoRepository.delete(demoId);

    // Then
    assertThat(demoRepository.existsById(demoId)).isFalse();
    assertThatThrownBy(() -> demoRepository.load(demoId))
        .isInstanceOf(DemoNotFoundException.class);
    assertThat(demoRepository.existsByName(DemoName.from("Delete_RoundTrip"))).isFalse();
  }

  private Demo newDemo(String name) {
    return Demo.of(
        DemoId.generate(),
        DemoName.from(name),
        DemoReference.fromIndex("001"),
        DemoDescription.from(name + "_Desc"));
  }
}

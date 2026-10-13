package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase.Command;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import cm.jemil.proxi.gaz.domain.demo.DemoReference;
import cm.jemil.proxi.gaz.domain.demo.DemoUpdatedEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateDemoUseCaseTest {

  private static final UUID DEMO_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final String NAME = "Updated Demo";
  private static final String DESCRIPTION = "Updated description";

  @Mock private DemoRepository demoRepository;
  @Mock private DomainEventPublisher domainEventPublisher;

  @InjectMocks private UpdateDemoUseCase updateDemoUseCase;

  @Captor private ArgumentCaptor<Demo> demoArgumentCaptor;
  @Captor private ArgumentCaptor<DemoUpdatedEvent> eventArgumentCaptor;

  @Test
  void execute_shouldLoadUpdateAndPersistDemo() {
    // Given
    Demo demo = existingDemo("Initial Demo");
    given(demoRepository.load(DemoId.from(DEMO_ID))).willReturn(demo);

    // When
    updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION));

    // Then
    then(demoRepository).should().update(demoArgumentCaptor.capture());
    assertThat(demoArgumentCaptor.getValue().getName()).isEqualTo(DemoName.from(NAME));
    assertThat(demoArgumentCaptor.getValue().getDescription())
        .isEqualTo(DemoDescription.from(DESCRIPTION));
  }

  @Test
  void execute_shouldKeepReferenceWhenUpdating() {
    // Given
    Demo demo = existingDemo("Initial Demo");
    given(demoRepository.load(DemoId.from(DEMO_ID))).willReturn(demo);

    // When
    updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION));

    // Then
    then(demoRepository).should().update(demoArgumentCaptor.capture());
    assertThat(demoArgumentCaptor.getValue().getReference()).isEqualTo(DemoReference.fromIndex("001"));
    assertThat(demoArgumentCaptor.getValue().getId()).isEqualTo(DemoId.from(DEMO_ID));
  }

  @Test
  void execute_shouldNotCheckNameConflict_whenNameIsUnchanged() {
    // Given
    Demo demo = existingDemo(NAME);
    given(demoRepository.load(DemoId.from(DEMO_ID))).willReturn(demo);

    // When
    updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION));

    // Then
    then(demoRepository).should().update(any(Demo.class));
    then(demoRepository).should(never()).existsByNameAndIdNot(any(), any());
  }

  @Test
  void execute_shouldCheckNameConflict_whenNameChangedAndNameIsFree() {
    // Given
    Demo demo = existingDemo("Initial Demo");
    given(demoRepository.load(DemoId.from(DEMO_ID))).willReturn(demo);
    given(demoRepository.existsByNameAndIdNot(NAME, DemoId.from(DEMO_ID))).willReturn(false);

    // When
    updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION));

    // Then
    then(demoRepository).should().existsByNameAndIdNot(NAME, DemoId.from(DEMO_ID));
    then(demoRepository).should().update(any(Demo.class));
  }

  @Test
  void execute_shouldThrowAlreadyExists_whenNameUsedByAnotherDemo() {
    // Given
    Demo demo = existingDemo("Initial Demo");
    given(demoRepository.load(DemoId.from(DEMO_ID))).willReturn(demo);
    given(demoRepository.existsByNameAndIdNot(NAME, DemoId.from(DEMO_ID))).willReturn(true);

    // When / Then
    assertThatThrownBy(() -> updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION)))
        .isInstanceOf(DemoAlreadyExistsException.class);
    then(demoRepository).should(never()).update(any(Demo.class));
    then(domainEventPublisher).should(never()).publishDemoUpdatedEvent(any());
  }

  @Test
  void execute_shouldPropagateNotFound_whenDemoDoesNotExist() {
    // Given
    given(demoRepository.load(DemoId.from(DEMO_ID))).willThrow(new DemoNotFoundException());

    // When / Then
    assertThatThrownBy(() -> updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION)))
        .isInstanceOf(DemoNotFoundException.class);
    then(demoRepository).should(never()).update(any(Demo.class));
  }

  @Test
  void execute_shouldPublishUpdatedEvent() {
    // Given
    Demo demo = existingDemo("Initial Demo");
    given(demoRepository.load(DemoId.from(DEMO_ID))).willReturn(demo);
    given(demoRepository.existsByNameAndIdNot(NAME, DemoId.from(DEMO_ID))).willReturn(false);

    // When
    updateDemoUseCase.execute(new Command(DEMO_ID, NAME, DESCRIPTION));

    // Then
    then(domainEventPublisher).should().publishDemoUpdatedEvent(eventArgumentCaptor.capture());
    DemoUpdatedEvent event = eventArgumentCaptor.getValue();
    assertThat(event.id()).isEqualTo(DemoId.from(DEMO_ID));
    assertThat(event.name()).isEqualTo(DemoName.from(NAME));
    assertThat(event.description()).isEqualTo(DemoDescription.from(DESCRIPTION));
  }

  private Demo existingDemo(String name) {
    return Demo.of(
        DemoId.from(DEMO_ID),
        DemoName.from(name),
        DemoReference.fromIndex("001"),
        DemoDescription.from("Initial description"));
  }
}

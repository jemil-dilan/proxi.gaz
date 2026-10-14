/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase.Command;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoCreatedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateDemoUseCaseTest {

  private static final String NAME = "Initial Demo";
  private static final String DESCRIPTION = "Initial description";
  private static final String REFERENCE_INDEX = "001";

  @Mock private ReferenceGenerator referenceGenerator;
  @Mock private DemoRepository demoRepository;
  @Mock private DomainEventPublisher domainEventPublisher;

  @InjectMocks private CreateDemoUseCase createDemoUseCase;

  @Captor private ArgumentCaptor<Demo> demoArgumentCaptor;
  @Captor private ArgumentCaptor<DemoCreatedEvent> eventArgumentCaptor;

  @Test
  void execute_shouldInsertPersistedDemoIntoRepository() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn(REFERENCE_INDEX);

    // When
    createDemoUseCase.execute(validCommand());

    // Then
    then(demoRepository).should().insert(demoArgumentCaptor.capture());
    assertThat(demoArgumentCaptor.getValue()).isNotNull();
  }

  @Test
  void execute_shouldReturnIdMatchingInsertedAggregate() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn(REFERENCE_INDEX);

    // When
    UUID result = createDemoUseCase.execute(validCommand());

    // Then
    then(demoRepository).should().insert(demoArgumentCaptor.capture());
    assertThat(demoArgumentCaptor.getValue().getId()).isEqualTo(DemoId.from(result));
  }

  @Test
  void execute_shouldBuildReferenceFromGeneratedIndex() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn("042");

    // When
    createDemoUseCase.execute(validCommand());

    // Then
    then(demoRepository).should().insert(demoArgumentCaptor.capture());
    assertThat(demoArgumentCaptor.getValue().getReference().value()).isEqualTo("DEMO-042");
  }

  @Test
  void execute_shouldPopulateAggregateWithCommandNameAndDescription() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn(REFERENCE_INDEX);

    // When
    createDemoUseCase.execute(validCommand());

    // Then
    then(demoRepository).should().insert(demoArgumentCaptor.capture());
    Demo demo = demoArgumentCaptor.getValue();
    assertThat(demo.getName()).isEqualTo(DemoName.from(NAME));
    assertThat(demo.getDescription()).isEqualTo(DemoDescription.from(DESCRIPTION));
  }

  @Test
  void execute_shouldGenerateNewDemoIdForEachInvocation() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn(REFERENCE_INDEX);

    // When
    createDemoUseCase.execute(validCommand());
    createDemoUseCase.execute(validCommand());

    // Then
    then(demoRepository).should(times(2)).insert(demoArgumentCaptor.capture());
    List<Demo> allInserted = demoArgumentCaptor.getAllValues();
    assertThat(allInserted.get(0).id()).isNotEqualTo(allInserted.get(1).id());
  }

  @Test
  void execute_shouldPublishEventMatchingInsertedAggregate() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn(REFERENCE_INDEX);

    // When
    UUID returnedId = createDemoUseCase.execute(validCommand());

    // Then
    then(demoRepository).should().insert(demoArgumentCaptor.capture());
    then(domainEventPublisher).should().publishDemoCreatedEvent(eventArgumentCaptor.capture());
    Demo demo = demoArgumentCaptor.getValue();
    DemoCreatedEvent event = eventArgumentCaptor.getValue();
    assertThat(event.id().value()).isEqualTo(returnedId);
    assertThat(event.id()).isEqualTo(demo.getId());
    assertThat(event.name()).isEqualTo(demo.getName());
    assertThat(event.reference()).isEqualTo(demo.getReference());
    assertThat(event.description()).isEqualTo(demo.getDescription());
  }

  @Test
  void execute_shouldPublishEventAfterDemoIsInserted() {
    // Given
    given(referenceGenerator.generateForDemo()).willReturn(REFERENCE_INDEX);

    // When
    createDemoUseCase.execute(validCommand());

    // Then
    var inOrder = org.mockito.Mockito.inOrder(demoRepository, domainEventPublisher);
    inOrder.verify(demoRepository).insert(any(Demo.class));
    inOrder.verify(domainEventPublisher).publishDemoCreatedEvent(any());
  }

  @Test
  void execute_shouldThrowAlreadyExists_whenNameAlreadyExists() {
    // Given
    given(demoRepository.existsByName(DemoName.from(NAME))).willReturn(true);

    // When / Then
    assertThatThrownBy(() -> createDemoUseCase.execute(validCommand()))
        .isInstanceOf(DemoAlreadyExistsException.class);
    then(demoRepository).should(never()).insert(any(Demo.class));
    then(domainEventPublisher).should(never()).publishDemoCreatedEvent(any());
  }

  @Test
  void createDemoCommand_recordShouldExposeAllFields() {
    // Given
    var command = new Command(NAME, DESCRIPTION);

    // Then
    assertThat(command.name()).isEqualTo(NAME);
    assertThat(command.description()).isEqualTo(DESCRIPTION);
  }

  private Command validCommand() {
    return new Command(NAME, DESCRIPTION);
  }
}

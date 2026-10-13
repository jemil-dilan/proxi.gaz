/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.usecase.DeleteDemoUseCase.Command;
import cm.jemil.proxi.gaz.domain.demo.DemoDeletedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteDemoUseCaseTest {

  private static final UUID DEMO_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  @Mock private DemoRepository demoRepository;
  @Mock private DomainEventPublisher domainEventPublisher;

  @InjectMocks private DeleteDemoUseCase deleteDemoUseCase;

  @Captor private ArgumentCaptor<DemoDeletedEvent> eventArgumentCaptor;

  @Test
  void execute_shouldDeleteExistingDemo() {
    // Given
    given(demoRepository.existsById(DemoId.from(DEMO_ID))).willReturn(true);

    // When
    deleteDemoUseCase.execute(new Command(DEMO_ID));

    // Then
    then(demoRepository).should().delete(DemoId.from(DEMO_ID));
  }

  @Test
  void execute_shouldPublishDeletedEvent() {
    // Given
    given(demoRepository.existsById(DemoId.from(DEMO_ID))).willReturn(true);

    // When
    deleteDemoUseCase.execute(new Command(DEMO_ID));

    // Then
    then(domainEventPublisher).should().publishDemoDeletedEvent(eventArgumentCaptor.capture());
    assertThat(eventArgumentCaptor.getValue().id()).isEqualTo(DemoId.from(DEMO_ID));
  }

  @Test
  void execute_shouldThrowNotFound_whenDemoDoesNotExist() {
    // Given
    given(demoRepository.existsById(DemoId.from(DEMO_ID))).willReturn(false);

    // When / Then
    assertThatThrownBy(() -> deleteDemoUseCase.execute(new Command(DEMO_ID)))
        .isInstanceOf(DemoNotFoundException.class);
    then(demoRepository).should(never()).delete(DemoId.from(DEMO_ID));
    then(domainEventPublisher)
        .should(never())
        .publishDemoDeletedEvent(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void deleteCommand_recordShouldExposeAllFields() {
    // Given
    var command = new Command(DEMO_ID);

    // Then
    assertThat(command.demoId()).isEqualTo(DEMO_ID);
  }
}

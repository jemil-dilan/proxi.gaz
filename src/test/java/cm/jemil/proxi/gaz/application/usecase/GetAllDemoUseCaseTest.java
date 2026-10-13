package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.domain.Constants;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAllDemoUseCaseTest {

  @InjectMocks private GetAllDemoUseCase getAllDemoUseCase;

  @Mock private DemoRepository demoRepository;

  @Test
  void execute_shouldReturnAllDemos_whenFieldsToExtractCodeIsOne() {
    // Given
    DemoView1 view1 = mock(DemoView1.class);
    DemoView1 view2 = mock(DemoView1.class);
    var demoViews = List.of(view1, view2);
    given(demoRepository.loadAllAsView1()).willReturn(demoViews);

    // When
    var result = getAllDemoUseCase.execute(Constants.FIELDS_TO_EXTRACT_1);

    // Then
    assertThat(result).containsExactly(view1, view2);
    then(demoRepository).should().loadAllAsView1();
  }

  @Test
  void execute_shouldReturnEmptyList_whenRepositoryReturnsEmpty() {
    // Given
    given(demoRepository.loadAllAsView1()).willReturn(List.of());

    // When
    var result = getAllDemoUseCase.execute(Constants.FIELDS_TO_EXTRACT_1);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void execute_shouldThrowFieldsExtractionCodeNotSupported_whenCodeIsUnsupported() {
    // When / Then
    assertThatThrownBy(() -> getAllDemoUseCase.execute("UNSUPPORTED_CODE"))
        .isInstanceOf(FieldsExtractionCodeNotSupported.class);
    then(demoRepository).should(never()).loadAllAsView1();
  }

  @Test
  void execute_shouldThrowFieldsExtractionCodeNotSupported_whenCodeIsEmpty() {
    // When / Then
    assertThatThrownBy(() -> getAllDemoUseCase.execute(""))
        .isInstanceOf(FieldsExtractionCodeNotSupported.class);
  }

  @Test
  void execute_shouldThrowNullPointerException_whenCodeIsNull() {
    // When / Then - switching on null raises NPE per JLS
    assertThatThrownBy(() -> getAllDemoUseCase.execute(null))
        .isInstanceOf(NullPointerException.class);
    then(demoRepository).should(never()).loadAllAsView1();
  }
}

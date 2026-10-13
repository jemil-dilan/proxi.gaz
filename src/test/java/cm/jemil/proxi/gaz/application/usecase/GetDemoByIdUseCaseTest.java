package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.usecase.GetDemoByIdUseCase.Query;
import cm.jemil.proxi.gaz.application.views.DemoView;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import cm.jemil.proxi.gaz.domain.Constants;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetDemoByIdUseCaseTest {

  private static final UUID DEMO_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  @InjectMocks private GetDemoByIdUseCase getDemoByIdUseCase;

  @Mock private DemoRepository demoRepository;

  @Test
  void execute_shouldReturnView1_whenFieldsToExtractCodeIsOne() {
    // Given
    DemoView1 view1 = mock(DemoView1.class);
    given(demoRepository.loadAsView1(DemoId.from(DEMO_ID))).willReturn(view1);

    // When
    DemoView result =
        getDemoByIdUseCase.execute(new Query(DEMO_ID, Constants.FIELDS_TO_EXTRACT_1));

    // Then
    assertThat(result).isSameAs(view1);
    then(demoRepository).should().loadAsView1(DemoId.from(DEMO_ID));
    then(demoRepository).should(never()).loadAsView2(DemoId.from(DEMO_ID));
  }

  @Test
  void execute_shouldReturnView2_whenFieldsToExtractCodeIsTwo() {
    // Given
    DemoView2 view2 = mock(DemoView2.class);
    given(demoRepository.loadAsView2(DemoId.from(DEMO_ID))).willReturn(view2);

    // When
    DemoView result =
        getDemoByIdUseCase.execute(new Query(DEMO_ID, Constants.FIELDS_TO_EXTRACT_2));

    // Then
    assertThat(result).isSameAs(view2);
    then(demoRepository).should().loadAsView2(DemoId.from(DEMO_ID));
    then(demoRepository).should(never()).loadAsView1(DemoId.from(DEMO_ID));
  }

  @Test
  void execute_shouldThrowFieldsExtractionCodeNotSupported_whenCodeIsUnsupported() {
    // When / Then
    assertThatThrownBy(() -> getDemoByIdUseCase.execute(new Query(DEMO_ID, "UNSUPPORTED_CODE")))
        .isInstanceOf(FieldsExtractionCodeNotSupported.class);
    then(demoRepository).should(never()).loadAsView1(DemoId.from(DEMO_ID));
    then(demoRepository).should(never()).loadAsView2(DemoId.from(DEMO_ID));
  }

  @Test
  void execute_shouldThrowNullPointerException_whenDemoIdIsNull() {
    // When / Then
    assertThatThrownBy(
            () -> getDemoByIdUseCase.execute(new Query(null, Constants.FIELDS_TO_EXTRACT_1)))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void query_recordShouldExposeAllFields() {
    // Given
    var query = new Query(DEMO_ID, Constants.FIELDS_TO_EXTRACT_1);

    // Then
    assertThat(query.demoId()).isEqualTo(DEMO_ID);
    assertThat(query.fieldsToExtractCode()).isEqualTo(Constants.FIELDS_TO_EXTRACT_1);
  }
}

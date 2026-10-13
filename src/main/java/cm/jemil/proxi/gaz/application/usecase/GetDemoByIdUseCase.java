package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.views.DemoView;
import cm.jemil.proxi.gaz.domain.Constants;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetDemoByIdUseCase {

  private final DemoRepository demoRepository;

  public DemoView execute(Query input) {
    var demoId = Objects.requireNonNull(input.demoId);
    return switch (input.fieldsToExtractCode) {
      case Constants.FIELDS_TO_EXTRACT_1 -> demoRepository.loadAsView1(DemoId.from(demoId));
      case Constants.FIELDS_TO_EXTRACT_2 -> demoRepository.loadAsView2(DemoId.from(demoId));
      default -> throw new FieldsExtractionCodeNotSupported();
    };
  }

  public record Query(UUID demoId, String fieldsToExtractCode) {}
}

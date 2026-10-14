/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.domain.Constants;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAllDemoUseCase {

  private final DemoRepository demoRepository;

  public List<DemoView1> execute(String fieldsToExtractCode) {
    return switch (fieldsToExtractCode) {
      case Constants.FIELDS_TO_EXTRACT_1 -> demoRepository.loadAllAsView1();
      default -> throw new FieldsExtractionCodeNotSupported();
    };
  }
}

/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.DeleteDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetDemoByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetDemoByIdUseCase.Query;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.DemoApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreationResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoCreateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoUpdateDTO;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class DemoController implements DemoApi {

  private final RestMapper restMapper;
  private final CreateDemoUseCase createDemoUseCase;
  private final GetAllDemoUseCase getAllDemoUseCase;
  private final GetDemoByIdUseCase getDemoByIdUseCase;
  private final UpdateDemoUseCase updateDemoUseCase;
  private final DeleteDemoUseCase deleteDemoUseCase;

  @Override
  public ResponseEntity<CreationResponseDTO> createDemo(DemoCreateDTO demoCreateDTO) {
    var newId = createDemoUseCase.execute(restMapper.toCommand(demoCreateDTO));
    return ResponseEntity.status(CREATED).body(restMapper.fromDomain(newId));
  }

  @Override
  public ResponseEntity<List<DemoDTO>> fetchAllDemo(String fieldsToExtractCode) {
    var result =
        getAllDemoUseCase.execute(fieldsToExtractCode).stream()
            .map(restMapper::fromDomain)
            .toList();
    return ResponseEntity.status(OK).body(result);
  }

  @Override
  public ResponseEntity<DemoDTO> fetchDemoById(UUID demoId, String fieldsToExtractCode) {
    var response = getDemoByIdUseCase.execute(new Query(demoId, fieldsToExtractCode));
    return ResponseEntity.ok(restMapper.fromDomain(response));
  }

  @Override
  public ResponseEntity<Void> updateDemo(UUID demoId, DemoUpdateDTO demoUpdateDTO) {
    updateDemoUseCase.execute(restMapper.toCommand(demoId, demoUpdateDTO));
    return ResponseEntity.status(NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> deleteDemo(UUID demoId) {
    deleteDemoUseCase.execute(new DeleteDemoUseCase.Command(demoId));
    return ResponseEntity.status(NO_CONTENT).build();
  }
}

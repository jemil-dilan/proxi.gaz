/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.VendorApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorUpdateDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class VendorController implements VendorApi {

  private final RestMapper restMapper;
  private final CreateVendorUseCase createVendorUseCase;
  private final UpdateVendorUseCase updateVendorUseCase;

  @Override
  public ResponseEntity<VendorOnboardingResponseDTO> createVendor(CreateVendorDTO request) {
    var result = createVendorUseCase.execute(restMapper.toCommand(request));
    return ResponseEntity.status(201)
        .cacheControl(CacheControl.noStore())
        .body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<Void> updateVendor(UUID vendorId, VendorUpdateDTO request) {
    updateVendorUseCase.execute(restMapper.toCommand(vendorId, request));
    return ResponseEntity.noContent().build();
  }
}

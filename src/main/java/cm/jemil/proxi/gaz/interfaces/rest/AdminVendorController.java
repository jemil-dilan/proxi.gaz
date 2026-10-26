/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.AdminVendorApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreationResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorDetailsDTO;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdminVendorController implements AdminVendorApi {
  private final RestMapper mapper;
  private final CreateVendorUseCase createVendor;
  private final UpdateVendorUseCase updateVendor;

  AdminVendorController(
      RestMapper mapper, CreateVendorUseCase createVendor, UpdateVendorUseCase updateVendor) {
    this.mapper = mapper;
    this.createVendor = createVendor;
    this.updateVendor = updateVendor;
  }

  @Override
  public ResponseEntity<CreationResponseDTO> createVendor(VendorDetailsDTO request) {
    var result = createVendor.execute(mapper.toCreateVendorCommand(request));
    return ResponseEntity.status(201).body(mapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<Void> updateVendor(UUID vendorId, VendorDetailsDTO request) {
    updateVendor.execute(mapper.toUpdateVendorCommand(vendorId, request));
    return ResponseEntity.noContent().build();
  }
}

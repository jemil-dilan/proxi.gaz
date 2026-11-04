/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.ListVendorsUseCase;
import cm.jemil.proxi.gaz.application.usecase.RevokeVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RotateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.application.views.VendorView;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.config.VendorPrincipal;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.VendorApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorPaginationDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorSessionIdentityDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorSessionTokenDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorStatusUpdateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorUpdateDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class VendorController implements VendorApi {

  private final RestMapper restMapper;
  private final CreateVendorUseCase createVendorUseCase;
  private final GetVendorUseCase getVendorUseCase;
  private final UpdateVendorUseCase updateVendorUseCase;
  private final SetVendorStatusUseCase setVendorStatusUseCase;
  private final ListVendorsUseCase listVendorsUseCase;
  private final RotateVendorSessionUseCase rotateVendorSessionUseCase;
  private final RevokeVendorSessionUseCase revokeVendorSessionUseCase;
  private final VendorRepository vendorRepository;

  @Override
  public ResponseEntity<VendorOnboardingResponseDTO> createVendor(CreateVendorDTO request) {
    var result = createVendorUseCase.execute(restMapper.toCommand(request));
    return ResponseEntity.status(201)
        .cacheControl(CacheControl.noStore())
        .body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<VendorPaginationDTO> listVendors(
      Integer page, Integer size, Boolean isActive, String name, String sort) {
    var result =
        listVendorsUseCase.execute(
            new ListVendorsUseCase.Query(
                page == null ? 0 : page, size == null ? 20 : size, isActive, name));
    return ResponseEntity.ok().body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorDetailDTO> getVendor(
      UUID vendorId) {
    var result = getVendorUseCase.execute(VendorId.from(vendorId));
    return ResponseEntity.ok(restMapper.toDTO(result));
  }

  @Override
  public ResponseEntity<Void> updateVendor(UUID vendorId, VendorUpdateDTO request) {
    updateVendorUseCase.execute(restMapper.toCommand(vendorId, request));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> setVendorStatus(UUID vendorId, VendorStatusUpdateDTO request) {
    setVendorStatusUseCase.execute(VendorId.from(vendorId), request.getIsActive());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<VendorSessionIdentityDTO> getVendorSession() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    var vendorId =
        authentication != null && authentication.getPrincipal() instanceof VendorPrincipal(UUID id)
            ? id
            : null;

    if (vendorId == null) {
      throw new InvalidVendorSessionException();
    }

    // For PXG-058: return name and isActive from vendor
    var vendor = vendorRepository.findById(VendorId.from(vendorId));
    if (!vendor.isActive()) {
      throw new VendorInactiveException(VendorId.from(vendorId));
    }

    return ResponseEntity.ok(
        new VendorSessionIdentityDTO()
            .vendorId(vendorId)
            .name(vendor.getName().value())
            .isActive(vendor.isActive()));
  }

  @Override
  public ResponseEntity<Void> revokeVendorSession(UUID vendorId) {
    revokeVendorSessionUseCase.execute(vendorId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<VendorSessionTokenDTO> rotateVendorSession(UUID vendorId) {
    var token = rotateVendorSessionUseCase.execute(vendorId);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new VendorSessionTokenDTO(token));
  }
}

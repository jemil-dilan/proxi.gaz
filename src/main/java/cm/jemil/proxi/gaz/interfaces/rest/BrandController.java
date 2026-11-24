/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateBrandUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetBrandsUseCase;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.BrandApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.BrandCreateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.BrandDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class BrandController implements BrandApi {

  private final RestMapper restMapper;
  private final CreateBrandUseCase createBrandUseCase;
  private final GetBrandsUseCase getBrandsUseCase;

  @Override
  public ResponseEntity<BrandDTO> createBrand(BrandCreateDTO request) {
    var brand =
        createBrandUseCase.execute(
            AuthenticatedAdmin.currentAdminId(), BrandName.from(request.getName()));
    return ResponseEntity.status(HttpStatus.CREATED).body(restMapper.fromBrand(brand));
  }

  @Override
  public ResponseEntity<List<BrandDTO>> listBrands(@Nullable Boolean isActive) {
    return ResponseEntity.ok(
        getBrandsUseCase.execute(isActive).stream().map(restMapper::fromBrand).toList());
  }
}

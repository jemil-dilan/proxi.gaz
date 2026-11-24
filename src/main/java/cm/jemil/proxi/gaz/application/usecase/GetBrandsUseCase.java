/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class GetBrandsUseCase {
  private final BrandRepository brandRepository;

  public List<Brand> execute(@Nullable Boolean active) {
    return brandRepository.findAll(active);
  }
}

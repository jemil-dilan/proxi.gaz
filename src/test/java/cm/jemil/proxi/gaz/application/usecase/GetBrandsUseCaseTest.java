/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetBrandsUseCaseTest {

  private final BrandRepository brandRepository = mock(BrandRepository.class);
  private final GetBrandsUseCase useCase = new GetBrandsUseCase(brandRepository);

  @Test
  void execute_shouldReturnTheRepositoryListForTheFilter() {
    var brands = List.of(Brand.create(BrandName.from("Tradex"), CreatedAt.from(Instant.EPOCH)));
    when(brandRepository.findAll(true)).thenReturn(brands);

    assertThat(useCase.execute(true)).isSameAs(brands);
  }

  @Test
  void execute_shouldListEveryBrandWithoutFilter() {
    when(brandRepository.findAll(null)).thenReturn(List.of());

    assertThat(useCase.execute(null)).isEmpty();
  }
}

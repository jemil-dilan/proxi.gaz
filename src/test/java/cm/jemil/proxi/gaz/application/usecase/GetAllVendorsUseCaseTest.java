/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.views.VendorView;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import cm.jemil.proxi.gaz.domain.vendor.VendorListSort;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAllVendorsUseCaseTest {

  @InjectMocks private GetAllVendorsUseCase useCase;

  @Mock private VendorRepository repository;

  @Test
  void execute_shouldPassFiltersAndPageBoundsToRepository() {
    // Given
    var query = new GetAllVendorsUseCase.Query(1, 10, false, "Akwa", "name,desc");
    var pagedata = new PageData<VendorView.VendorView1>(List.of(), 15L, 2, 10, 1);
    given(
            repository.loadAllVendors(
                new PaginationFetchRequest(10, 1),
                false,
                new VendorName("Akwa"),
                new VendorListSort(VendorListSort.Field.NAME, VendorListSort.Direction.DESC)))
        .willReturn(pagedata);

    // When
    var response = useCase.execute(query);

    // Then
    assertThat(response.vendors()).isEmpty();
    assertThat(response.totalElements()).isEqualTo(15);
    assertThat(response.totalPages()).isEqualTo(2);
    assertThat(response.pageSize()).isEqualTo(10);
    assertThat(response.pageNumber()).isEqualTo(1);
    then(repository)
        .should()
        .loadAllVendors(
            new PaginationFetchRequest(10, 1),
            false,
            new VendorName("Akwa"),
            new VendorListSort(VendorListSort.Field.NAME, VendorListSort.Direction.DESC));
  }

  @Test
  void execute_withDefaultFilters_shouldPassNullActiveAndDefaultSort() {
    // Given
    var query = new GetAllVendorsUseCase.Query(0, 20, null, null, null);
    var pagedata = new PageData<VendorView.VendorView1>(List.of(), 0L, 0, 20, 0);
    given(
            repository.loadAllVendors(
                new PaginationFetchRequest(20, 0), null, null, VendorListSort.DEFAULT))
        .willReturn(pagedata);

    // When
    var response = useCase.execute(query);

    // Then
    assertThat(response.totalElements()).isEqualTo(0);
    then(repository)
        .should()
        .loadAllVendors(new PaginationFetchRequest(20, 0), null, null, VendorListSort.DEFAULT);
  }

  @Test
  void execute_withActiveTrue_shouldPassTrueToRepository() {
    // Given
    var query = new GetAllVendorsUseCase.Query(0, 20, true, null, "createdAt,asc");
    var pagedata = new PageData<VendorView.VendorView1>(List.of(), 5L, 1, 20, 0);
    given(
            repository.loadAllVendors(
                new PaginationFetchRequest(20, 0),
                true,
                null,
                new VendorListSort(VendorListSort.Field.CREATED_AT, VendorListSort.Direction.ASC)))
        .willReturn(pagedata);

    // When
    var response = useCase.execute(query);

    // Then
    assertThat(response.totalElements()).isEqualTo(5);
    then(repository)
        .should()
        .loadAllVendors(
            new PaginationFetchRequest(20, 0),
            true,
            null,
            new VendorListSort(VendorListSort.Field.CREATED_AT, VendorListSort.Direction.ASC));
  }
}

/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.views.VendorView;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class ListVendorsUseCase {
  private final VendorRepository vendorRepository;

  public Response execute(Query query) {
    var pagedata =
        vendorRepository.loadAllVendors(
            query.paginationFetchRequest(), query.isActive(), query.vendorName());
    return new Response(
        pagedata.elements(),
        pagedata.total(),
        pagedata.totalPages(),
        pagedata.pageSize(),
        pagedata.pageNumber());
  }

  public record Query(int page, int size, Boolean isActive, @Nullable String name) {
    private PaginationFetchRequest paginationFetchRequest() {
      return new PaginationFetchRequest(size, page);
    }

    private @Nullable VendorName vendorName() {
      return name == null ? null : new VendorName(name);
    }
  }

  public record Response(
      List<VendorView.VendorView1> vendors,
      long totalElements,
      int totalPages,
      int pageSize,
      int pageNumber) {}
}

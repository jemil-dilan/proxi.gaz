/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.views.VendorView.VendorView1;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorListSort;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaVendorRepository implements VendorRepository {

  private final VendorSpringRepository vendorSpringRepository;
  private final JpaMapper jpaMapper;

  @Override
  @Transactional(readOnly = true)
  public Vendor findById(VendorId id) {
    return vendorSpringRepository
        .findAggregateById(id.value())
        .map(jpaMapper::toDomain)
        .orElseThrow(VendorNotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Vendor> findByUserId(UserId userId) {
    return vendorSpringRepository.findAggregateByUserId(userId.value()).map(jpaMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public PageData<VendorView1> loadAllVendors(
      PaginationFetchRequest paginationFetchRequest,
      @Nullable Boolean isActive,
      @Nullable VendorName vendorName,
      VendorListSort sort) {
    var name = Optional.ofNullable(vendorName).map(VendorName::value).orElse(null);
    var pageable =
        PageRequest.of(
            paginationFetchRequest.page(), paginationFetchRequest.limit(), toSpringSort(sort));
    var page = vendorSpringRepository.loadAllVendorView(isActive, name, pageable);
    return new PageData<>(
        page.getContent(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getSize(),
        page.getNumber());
  }

  @Override
  @Transactional(readOnly = true)
  public VendorView1 findViewById(VendorId vendorId) {
    return vendorSpringRepository
        .findVendorViewById(vendorId.value())
        .orElseThrow(VendorNotFoundException::new);
  }

  @Override
  public void update(Vendor vendor) {
    vendorSpringRepository
        .findAggregateById(vendor.id())
        .map(
            vendorJpa -> {
              jpaMapper.fromDomain(vendor, vendorJpa);
              return vendorSpringRepository.save(vendorJpa);
            })
        .orElseThrow(VendorNotFoundException::new);
  }

  private static Sort toSpringSort(VendorListSort sort) {
    var direction =
        sort.direction() == VendorListSort.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    // Order by selected columns only (no LOWER(...)): Postgres rejects ORDER BY expressions that
    // are not in the SELECT list when DISTINCT is present, and LOWER would not be selected.
    var property =
        switch (sort.field()) {
          case NAME -> "v.name";
          case CREATED_AT -> "v.createdAt";
        };
    return JpaSort.unsafe(direction, property).and(JpaSort.unsafe("v.id"));
  }
}

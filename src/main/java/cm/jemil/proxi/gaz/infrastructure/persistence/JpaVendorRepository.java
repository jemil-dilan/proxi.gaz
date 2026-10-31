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
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
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
      boolean isActive,
      @Nullable VendorName vendorName) {
    var name = Optional.ofNullable(vendorName).map(VendorName::value).orElse(null);
    var pageable = PageRequest.of(paginationFetchRequest.page(), paginationFetchRequest.limit());
    var page = vendorSpringRepository.search(isActive, name, pageable);
    return new PageData<>(
        page.getContent().stream().map(jpaMapper::toView).toList(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getSize(),
        page.getNumber());
  }

  @Override
  public void update(Vendor vendor) {
    vendorSpringRepository
        .findAggregateById(vendor.id())
        .ifPresent(
            vendorJpa -> {
              jpaMapper.fromDomain(vendor, vendorJpa);
              vendorSpringRepository.save(vendorJpa);
            });
  }
}

/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaVendorSessionRepository implements VendorSessionRepository {
  private final VendorSessionSpringRepository springRepository;
  private final JpaMapper jpaMapper;

  @Override
  @Transactional(readOnly = true)
  public Optional<VendorSession> findByTokenHash(VendorSessionTokenHash tokenHash) {
    return springRepository.findByTokenHash(tokenHash.value()).map(jpaMapper::toDomain);
  }

  @Override
  public void replaceForVendor(VendorId vendorId, VendorSession session) {
    springRepository.deleteByVendorId(vendorId.value());
    springRepository.flush();
    // why not update
    springRepository.save(jpaMapper.toJpa(session));
  }

  @Override
  public void revokeForVendor(VendorId vendorId) {
    springRepository.deleteByVendorId(vendorId.value());
  }
}

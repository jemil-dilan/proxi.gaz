/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.shared.LastUsedAt;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
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
  public VendorSession findByTokenHash(VendorSessionTokenHash tokenHash) {
    return springRepository
        .findByTokenHash(tokenHash.value())
        .map(jpaMapper::toDomain)
        .orElseThrow(InvalidVendorSessionException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VendorSession> findByVendorId(VendorId vendorId) {
    return springRepository
        .findFirstActiveByVendorIdOrderByCreatedAtDesc(vendorId.value())
        .map(jpaMapper::toDomain);
  }

  @Override
  public void replaceForVendor(VendorId vendorId, VendorSession session) {
    // Soft-delete old sessions for this vendor
    springRepository.softDeleteByVendorId(vendorId.value());
    springRepository.flush();
    // Create new session
    springRepository.save(jpaMapper.toJpa(session));
  }

  @Override
  public void revokeForVendor(VendorId vendorId) {
    // Soft-delete all sessions for this vendor
    springRepository.softDeleteByVendorId(vendorId.value());
  }

  @Override
  public void updateLastUsedAt(VendorId vendorId, LastUsedAt lastUsedAt) {
    springRepository
        .findFirstActiveByVendorIdOrderByCreatedAtDesc(vendorId.value())
        .ifPresent(
            sessionJpa -> {
              sessionJpa.setLastUsedAt(lastUsedAt.value());
              springRepository.save(sessionJpa);
            });
  }
}

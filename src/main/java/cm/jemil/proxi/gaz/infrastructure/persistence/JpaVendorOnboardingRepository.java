/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaVendorOnboardingRepository implements VendorOnboardingRepository {

  private final UserSpringRepository userSpringRepository;
  private final VendorSpringRepository vendorSpringRepository;
  private final VendorSessionSpringRepository vendorSessionSpringRepository;
  private final JpaMapper jpaMapper;

  @Override
  public void insert(User user, Vendor vendor, VendorSession vendorSession) {
    userSpringRepository.saveAndFlush(jpaMapper.toJpa(user));
    vendorSpringRepository.saveAndFlush(jpaMapper.toJpa(vendor));
    vendorSessionSpringRepository.saveAndFlush(jpaMapper.toJpa(vendorSession));
  }
}

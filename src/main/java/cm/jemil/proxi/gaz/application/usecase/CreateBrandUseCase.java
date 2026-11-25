/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBrandUseCase {
  private final BrandRepository brandRepository;
  private final AuditLogPort auditLog;
  private final UnitOfWork unitOfWork;
  private final Clock clock;

  /** Adds an active brand; the name must be new ignoring case and spacing. */
  public Brand execute(UserId adminUserId, BrandName name) {
    var createdAt = CreatedAt.from(clock.now());
    var brand = Brand.create(name, createdAt);
    unitOfWork.inTransaction(
        () -> {
          if (brandRepository.existsByName(name)) {
            throw new BrandAlreadyExistsException();
          }
          brandRepository.insert(brand);
          auditLog.record(
              AuditEntry.brand(
                  adminUserId,
                  AuditAction.BRAND_CREATED,
                  brand.id().value(),
                  AuditMetadata.created(brand.active()),
                  createdAt));
          return brand;
        });
    return brand;
  }
}

/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * One transaction per unit of work. Repository adapters are {@code @Transactional} with the default
 * REQUIRED propagation, so they join it; any exception rolls every write back.
 */
@RequiredArgsConstructor
class SpringUnitOfWork implements UnitOfWork {

  private final TransactionTemplate transactionTemplate;

  @Override
  public <T> T inTransaction(Supplier<T> work) {
    return transactionTemplate.execute(status -> work.get());
  }
}

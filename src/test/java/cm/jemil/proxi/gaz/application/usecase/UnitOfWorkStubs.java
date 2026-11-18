/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import java.util.function.Supplier;

/** Mockito stubs for {@link UnitOfWork}; the real transaction is covered by the data tests. */
final class UnitOfWorkStubs {

  private UnitOfWorkStubs() {}

  /** A unit of work that runs the work immediately, as a committed transaction would. */
  static UnitOfWork runningWork() {
    var unitOfWork = mock(UnitOfWork.class);
    when(unitOfWork.inTransaction(any()))
        .thenAnswer(call -> call.<Supplier<?>>getArgument(0).get());
    return unitOfWork;
  }

  /** A unit of work that never runs the work: anything written outside it would still show. */
  static UnitOfWork notRunningWork() {
    return mock(UnitOfWork.class);
  }
}

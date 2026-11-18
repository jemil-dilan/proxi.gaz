/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import java.util.function.Supplier;

/**
 * Runs application work in one transaction: every repository call made inside joins it, and an
 * exception rolls all of them back. Keeps the transaction boundary in the application layer without
 * depending on Spring.
 */
public interface UnitOfWork {

  <T> T inTransaction(Supplier<T> work);
}

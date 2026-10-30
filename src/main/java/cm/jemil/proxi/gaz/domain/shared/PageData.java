/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import java.util.List;
import java.util.stream.Stream;

public record PageData<T>(
    List<T> elements, long total, int totalPages, int pageSize, int pageNumber) {

  public Stream<T> stream() {
    return elements.stream();
  }
}

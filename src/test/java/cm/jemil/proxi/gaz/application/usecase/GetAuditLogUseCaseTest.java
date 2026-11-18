/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.views.AuditLogView;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetAuditLogUseCaseTest {

  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final GetAuditLogUseCase useCase = new GetAuditLogUseCase(auditLog);

  @Test
  void execute_shouldQueryTheAuditLogWithTheTargetAndPage() {
    var targetId = UUID.randomUUID();
    var page = new PageData<AuditLogView>(List.of(), 0, 0, 5, 2);
    when(auditLog.find(AuditTargetType.VENDOR, targetId, new PaginationFetchRequest(5, 2)))
        .thenReturn(page);

    var result =
        useCase.execute(new GetAuditLogUseCase.Query(AuditTargetType.VENDOR, targetId, 2, 5));

    assertThat(result).isSameAs(page);
  }

  @Test
  void execute_shouldListEverythingWithoutFilters() {
    var page = new PageData<AuditLogView>(List.of(), 0, 0, 20, 0);
    when(auditLog.find(null, null, new PaginationFetchRequest(20, 0))).thenReturn(page);

    assertThat(useCase.execute(new GetAuditLogUseCase.Query(null, null, 0, 20))).isSameAs(page);
  }

  @Test
  void query_shouldRejectATargetIdWithoutATargetType() {
    var targetId = UUID.randomUUID();
    assertThatThrownBy(() -> new GetAuditLogUseCase.Query(null, targetId, 0, 20))
        .isInstanceOf(InvalidDomainValueException.class)
        .hasMessageContaining("targetType");
  }
}

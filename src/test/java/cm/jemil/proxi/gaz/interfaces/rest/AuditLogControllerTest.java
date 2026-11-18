/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.usecase.GetAuditLogUseCase;
import cm.jemil.proxi.gaz.application.views.AuditLogView;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.PageData;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AuditActionDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AuditTargetTypeDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuditLogControllerTest {

  private final GetAuditLogUseCase useCase = Mockito.mock(GetAuditLogUseCase.class);
  private final AuditLogController controller =
      new AuditLogController(new RestMapperImpl(), useCase);

  @Test
  void listAuditLog_shouldMapTheQueryAndThePage() {
    var adminId = UserId.generate();
    var vendorId = UUID.randomUUID();
    var entry =
        AuditEntry.vendor(
            adminId,
            AuditAction.VENDOR_UPDATED,
            vendorId,
            AuditMetadata.updated(List.of("name")),
            CreatedAt.from(Instant.parse("2026-11-18T10:15:30Z")));
    var query = new GetAuditLogUseCase.Query(AuditTargetType.VENDOR, vendorId, 1, 5);
    when(useCase.execute(query))
        .thenReturn(
            new PageData<>(List.of(new AuditLogView(entry, "admin@example.test")), 6, 2, 5, 1));

    var response = controller.listAuditLog(AuditTargetTypeDTO.VENDOR, vendorId, 1, 5);

    verify(useCase).execute(query);
    var body = response.getBody();
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(body.getPage()).isEqualTo(1);
    assertThat(body.getSize()).isEqualTo(5);
    assertThat(body.getTotalElements()).isEqualTo(6);
    assertThat(body.getTotalPages()).isEqualTo(2);
    var dto = body.getContent().getFirst();
    assertThat(dto.getId()).isEqualTo(entry.id().value());
    assertThat(dto.getAdminUserId()).isEqualTo(adminId.value());
    assertThat(dto.getAdminEmail()).isEqualTo("admin@example.test");
    assertThat(dto.getAction()).isEqualTo(AuditActionDTO.VENDOR_UPDATED);
    assertThat(dto.getTargetType()).isEqualTo(AuditTargetTypeDTO.VENDOR);
    assertThat(dto.getTargetId()).isEqualTo(vendorId);
    assertThat(dto.getMetadata()).containsEntry("changedFields", List.of("name"));
    assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.parse("2026-11-18T10:15:30"));
  }

  @Test
  void listAuditLog_shouldApplyDefaultPaging() {
    var query = new GetAuditLogUseCase.Query(null, null, 0, 20);
    when(useCase.execute(query)).thenReturn(new PageData<>(List.of(), 0, 0, 20, 0));

    var response = controller.listAuditLog(null, null, null, null);

    assertThat(response.getBody().getContent()).isEmpty();
  }
}

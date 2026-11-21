/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.GetAuditLogUseCase;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.AuditApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AuditLogPaginationDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AuditTargetTypeDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class AuditLogController implements AuditApi {

  private final RestMapper restMapper;
  private final GetAuditLogUseCase getAuditLogUseCase;

  @Override
  public ResponseEntity<AuditLogPaginationDTO> listAuditLog(
      @Nullable AuditTargetTypeDTO targetType,
      @Nullable UUID targetId,
      Integer page,
      Integer size) {
    var result =
        getAuditLogUseCase.execute(
            new GetAuditLogUseCase.Query(
                targetType == null ? null : AuditTargetType.valueOf(targetType.getValue()),
                targetId,
                page == null ? 0 : page,
                size == null ? 20 : size));
    return ResponseEntity.ok(restMapper.fromAuditLog(result));
  }
}

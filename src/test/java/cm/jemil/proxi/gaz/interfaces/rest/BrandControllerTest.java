/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.usecase.CreateBrandUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetBrandsUseCase;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.BrandCreateDTO;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class BrandControllerTest {

  private final CreateBrandUseCase createBrandUseCase = mock(CreateBrandUseCase.class);
  private final GetBrandsUseCase getBrandsUseCase = mock(GetBrandsUseCase.class);
  private final BrandController controller =
      new BrandController(new RestMapperImpl(), createBrandUseCase, getBrandsUseCase);

  private static final Brand TRADEX =
      Brand.create(BrandName.from("Tradex"), CreatedAt.from(Instant.parse("2026-11-24T09:15:30Z")));

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createBrand_shouldPassTheAdminAndTheNormalisedNameAndReturn201() {
    var adminId = UUID.randomUUID();
    authenticateAdmin(adminId.toString());
    var name = BrandName.from("  Tradex ");
    when(createBrandUseCase.execute(UserId.from(adminId), name)).thenReturn(TRADEX);

    var response = controller.createBrand(new BrandCreateDTO("  Tradex "));

    assertThat(response.getStatusCode().value()).isEqualTo(201);
    var body = response.getBody();
    assertThat(body.getId()).isEqualTo(TRADEX.id().value());
    assertThat(body.getName()).isEqualTo("Tradex");
    assertThat(body.getIsActive()).isTrue();
    assertThat(body.getCreatedAt()).isEqualTo(LocalDateTime.parse("2026-11-24T09:15:30"));
  }

  @Test
  void createBrand_shouldRejectABlankNameBeforeCallingTheUseCase() {
    authenticateAdmin(UUID.randomUUID().toString());
    var request = new BrandCreateDTO("   ");

    assertThatThrownBy(() -> controller.createBrand(request))
        .isInstanceOf(InvalidDomainValueException.class);
    verifyNoInteractions(createBrandUseCase);
  }

  @Test
  void listBrands_shouldMapTheFilteredCatalogue() {
    when(getBrandsUseCase.execute(true)).thenReturn(List.of(TRADEX));

    var response = controller.listBrands(true);

    verify(getBrandsUseCase).execute(true);
    assertThat(response.getBody()).extracting("name").containsExactly("Tradex");
  }

  private static void authenticateAdmin(String subject) {
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "HS256")
            .subject(subject)
            .claim("scope", "admin")
            .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }
}

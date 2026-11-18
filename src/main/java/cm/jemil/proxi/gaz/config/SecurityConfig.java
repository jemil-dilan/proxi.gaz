/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import cm.jemil.proxi.gaz.application.usecase.UpdateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.ValidateVendorSessionUseCase;
import cm.jemil.proxi.gaz.interfaces.rest.Constants;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Public, unauthenticated endpoints: health always; API docs and Swagger UI only when springdoc
   * enables them for the active profile (local and staging, never production).
   */
  @Order(0)
  @Bean
  public SecurityFilterChain publicEndpoints(
      HttpSecurity http,
      @Value("${springdoc.api-docs.enabled:false}") boolean apiDocsEnabled,
      @Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerUiEnabled) {
    var paths = new ArrayList<String>(List.of("/actuator/health/**"));
    if (apiDocsEnabled) {
      paths.addAll(List.of("/v3/api-docs", "/v3/api-docs/**"));
    }
    if (swaggerUiEnabled) {
      paths.addAll(List.of("/swagger-ui.html", "/swagger-ui/**"));
    }
    return http.securityMatcher(paths.toArray(String[]::new))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }

  @SuppressWarnings("java:S4502")
  @Order(1)
  @Bean
  public SecurityFilterChain vendorSecurityFilterChain(
      HttpSecurity http,
      ValidateVendorSessionUseCase validateVendorSessionUseCase,
      ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint,
      UpdateVendorSessionUseCase updateVendorSessionUseCase,
      ObjectMapper objectMapper) {
    http.securityMatcher(Constants.PATH_VENDOR_SESSION)
        .csrf(csrf -> csrf.requireCsrfProtectionMatcher(_ -> false))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().hasAuthority(Scopes.VENDOR))
        .addFilterBefore(
            new VendorSessionAuthenticationFilter(
                validateVendorSessionUseCase, updateVendorSessionUseCase, objectMapper),
            AnonymousAuthenticationFilter.class)
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));
    return http.build();
  }

  @SuppressWarnings("java:S4502")
  @Order(2)
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint) {
    http.csrf(csrf -> csrf.requireCsrfProtectionMatcher(_ -> false))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, Constants.PATH_ADMIN_LOGIN)
                    .permitAll()
                    .requestMatchers(Constants.PATH_ADMIN_AUTH_ROLE)
                    .hasAuthority(Scopes.ADMIN)
                    .requestMatchers(Constants.PATH_ADMIN_VENDORS)
                    .hasAuthority(Scopes.ADMIN)
                    .requestMatchers(HttpMethod.GET, Constants.PATH_DEMO)
                    .hasAnyAuthority(Scopes.DEMO_READ_ALL)
                    .requestMatchers(HttpMethod.POST, Constants.PATH_DEMO)
                    .hasAnyAuthority(Scopes.DEMO_CREATE)
                    .requestMatchers(HttpMethod.GET, Constants.PATH_DEMO_ID)
                    .hasAnyAuthority(Scopes.DEMO_READ)
                    .requestMatchers(HttpMethod.PUT, Constants.PATH_DEMO_ID)
                    .hasAnyAuthority(Scopes.DEMO_UPDATE)
                    .requestMatchers(HttpMethod.DELETE, Constants.PATH_DEMO_ID)
                    .hasAnyAuthority(Scopes.DEMO_DELETE)
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

    return http.build();
  }
}

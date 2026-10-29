/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import cm.jemil.proxi.gaz.interfaces.rest.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Order(0)
  @Bean
  public SecurityFilterChain healthEndpoints(HttpSecurity http) {
    return http.securityMatcher("/actuator/health/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }

  @SuppressWarnings("java:S4502")
  @Order(1)
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, Constants.PATH_ADMIN_LOGIN)
                    .permitAll()
                    .requestMatchers(Constants.PATH_ADMIN_AUTH)
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
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

    return http.build();
  }
}

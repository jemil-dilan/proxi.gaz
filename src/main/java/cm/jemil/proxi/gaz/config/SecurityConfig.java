/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.application.outbound.JwtSettings;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.infrastructure.security.BCryptPasswordVerifier;
import cm.jemil.proxi.gaz.infrastructure.security.NimbusAccessTokenIssuer;
import cm.jemil.proxi.gaz.infrastructure.security.ProxigazJwtSettings;
import cm.jemil.proxi.gaz.interfaces.rest.Constants;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtSettings jwtSettings(Configurations configuration) {
    return new ProxigazJwtSettings(configuration);
  }

  @Bean
  public JwtDecoder jwtDecoder(JwtSettings jwtSettings) {
    return NimbusJwtDecoder.withSecretKey(jwtSecretKey(jwtSettings)).build();
  }

  @Bean
  public JwtEncoder jwtEncoder(JwtSettings jwtSettings) {
    var jwkSource = new ImmutableSecret<>(jwtSecretKey(jwtSettings));
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public PasswordVerifier passwordVerifier(PasswordEncoder passwordEncoder) {
    return new BCryptPasswordVerifier(passwordEncoder);
  }

  @Bean
  public AccessTokenIssuer accessTokenIssuer(
      JwtEncoder jwtEncoder, Clock clock, JwtSettings jwtSettings) {
    return new NimbusAccessTokenIssuer(jwtEncoder, clock, jwtSettings);
  }

  private SecretKeySpec jwtSecretKey(JwtSettings jwtSettings) {
    String secret = jwtSettings.signingSecret();
    return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

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

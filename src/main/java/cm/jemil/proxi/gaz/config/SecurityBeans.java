/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.JwtSettings;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.infrastructure.security.BCryptPasswordVerifier;
import cm.jemil.proxi.gaz.infrastructure.security.NimbusAccessTokenIssuer;
import cm.jemil.proxi.gaz.infrastructure.security.ProxigazJwtSettings;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class SecurityBeans {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtSettings jwtSettings(ProxigazProperties properties) {
    return new ProxigazJwtSettings(properties.security().jwt());
  }

  @Bean
  public JwtDecoder jwtDecoder(JwtSettings jwtSettings) {
    return NimbusJwtDecoder.withSecretKey(jwtSecretKey(jwtSettings)).build();
  }

  @Bean
  public JwtEncoder jwtEncoder(JwtSettings jwtSettings) {
    var jwkSource = new ImmutableSecret<SecurityContext>(jwtSecretKey(jwtSettings));
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
}

package cm.jemil.proxi.gaz.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import cm.jemil.proxi.gaz.domain.Constants;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public JwtDecoder jwtDecoder(@Value("${proxigaz.security.jwt.secret}") String secret) {
    return NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
        .build();
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

package cm.jemil.proxi.gaz.e2e.steps;

import cm.jemil.proxi.gaz.Application;
import cm.jemil.proxi.gaz.e2e.HttpClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@ActiveProfiles("e2e")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {Application.class, CucumberSpringConfiguration.TestConfig.class})
public class CucumberSpringConfiguration {

  @MockitoBean private JwtDecoder jwtDecoder;

  @TestConfiguration(proxyBeanMethods = false)
  static class TestConfig {

    @Bean
    HttpClient httpClient() {
      return new HttpClient();
    }
  }
}

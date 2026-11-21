/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Guards the error-code registry in {@code docs/ERROR_CODES.md} against drift from {@link
 * ErrorCodes}. Every documented code must be declared in the enum and vice versa (PXG-055
 * acceptance).
 */
class ErrorCodesDocumentationTest {
  private static final Path REGISTRY = Path.of("docs", "ERROR_CODES.md");
  private static final Pattern REGISTRY_ROW = Pattern.compile("^\\|\\s*`(PROXIGAZ-\\d{3}-\\d{3})`");

  @Test
  void registryAndEnumShouldDeclareTheSameCodes() throws IOException {
    var documented = documentedCodes();
    var registered =
        Arrays.stream(ErrorCodes.values()).map(ErrorCodes::value).collect(Collectors.toSet());

    assertThat(documented).containsExactlyInAnyOrderElementsOf(registered);
  }

  private Set<String> documentedCodes() throws IOException {
    assertThat(REGISTRY).as("error-code registry").exists();

    try (var lines = Files.lines(REGISTRY)) {
      return lines
          .map(REGISTRY_ROW::matcher)
          .filter(Matcher::find)
          .map(matcher -> matcher.group(1))
          .collect(Collectors.toSet());
    }
  }
}

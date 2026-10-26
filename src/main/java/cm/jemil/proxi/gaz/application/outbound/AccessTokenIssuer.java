/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.Set;

public interface AccessTokenIssuer {

  IssuedAccessToken issue(UserId userId, Email email, Set<String> scopes);

  record IssuedAccessToken(String value, long expiresInSeconds) {}
}

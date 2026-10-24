/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;

public interface AdminAccessTokenIssuer {

  IssuedAccessToken issue(UserId adminUserId, Email email);

  record IssuedAccessToken(String value, long expiresInSeconds) {}
}

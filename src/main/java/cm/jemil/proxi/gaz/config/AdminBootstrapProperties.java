/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proxigaz.admin.bootstrap")
public record AdminBootstrapProperties(boolean enabled, String email, String password) {}

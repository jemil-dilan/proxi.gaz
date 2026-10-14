/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.views;

import java.util.UUID;

public interface DemoView {

  interface DemoView1 extends DemoView {
    UUID getDemoId();

    String getName();

    String getReference();
  }

  interface DemoView2 extends DemoView {
    UUID getDemoId();

    String getName();

    String getReference();

    String getDescription();
  }
}

/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import java.util.List;

public interface DemoRepository {

  void insert(Demo value);

  List<DemoView1> loadAllAsView1();

  DemoView1 loadAsView1(DemoId demoId) throws DemoNotFoundException;

  DemoView2 loadAsView2(DemoId demoId) throws DemoNotFoundException;

  Demo load(DemoId demoId) throws DemoNotFoundException;

  boolean existsByNameAndIdNot(String name, DemoId demoId);

  boolean existsByName(DemoName name);

  boolean existsById(DemoId demoId);

  void delete(DemoId demoId);

  Demo update(Demo value);
}

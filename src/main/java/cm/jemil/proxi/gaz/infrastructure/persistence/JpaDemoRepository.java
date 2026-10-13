/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaDemoRepository implements DemoRepository {

  private final DemoSpringRepository demoSpringRepository;
  private final JpaMapper jpaMapper;

  @Override
  public void insert(Demo value) {
    demoSpringRepository.save(jpaMapper.fromDomain(value));
  }

  @Override
  public List<DemoView1> loadAllAsView1() {
    return demoSpringRepository.findAllAsView1();
  }

  @Override
  public DemoView1 loadAsView1(DemoId demoId) throws DemoNotFoundException {
    return demoSpringRepository.findByIdAsView1(demoId.value()).stream()
        .findFirst()
        .orElseThrow(DemoNotFoundException::new);
  }

  @Override
  public DemoView2 loadAsView2(DemoId demoId) throws DemoNotFoundException {
    return demoSpringRepository.findByIdAsView2(demoId.value()).stream()
        .findFirst()
        .orElseThrow(DemoNotFoundException::new);
  }

  @Override
  public Demo load(DemoId demoId) throws DemoNotFoundException {
    return demoSpringRepository
        .findById(demoId.value())
        .map(jpaMapper::toDomain)
        .orElseThrow(DemoNotFoundException::new);
  }

  @Override
  public boolean existsByNameAndIdNot(String name, DemoId demoId) {
    return demoSpringRepository.existsByNameAndIdNot(name, demoId.value());
  }

  @Override
  public boolean existsByName(DemoName name) {
    return demoSpringRepository.existsByName(name.value());
  }

  @Override
  public boolean existsById(DemoId demoId) {
    return demoSpringRepository.existsById(demoId.value());
  }

  @Override
  public void delete(DemoId demoId) {
    demoSpringRepository.markAsDeleted(demoId.value());
  }

  @Override
  public Demo update(Demo value) {
    return jpaMapper.toDomain(demoSpringRepository.save(jpaMapper.fromDomain(value)));
  }
}

/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DemoSpringRepository extends JpaRepository<DemoJpa, UUID> {

  @Query(
      """
          SELECT
                t.id AS demoId,
                t.name AS name,
                t.reference AS reference
          FROM DemoJpa t
          WHERE t.id = :demoId
      """)
  Optional<DemoView1> findByIdAsView1(@Param("demoId") UUID demoId);

  @Query(
      """
          SELECT
                t.id AS demoId,
                t.name AS name,
                t.reference AS reference
          FROM DemoJpa t
      """)
  List<DemoView1> findAllAsView1();

  @Query(
      """
          SELECT
                t.id AS demoId,
                t.name AS name,
                t.reference AS reference,
                t.description AS description
          FROM DemoJpa t
          WHERE t.id = :demoId
      """)
  Optional<DemoView2> findByIdAsView2(@Param("demoId") UUID demoId);

  boolean existsByNameAndIdNot(String name, UUID value);

  boolean existsByName(String value);

  @Modifying
  @Query(
      """
          UPDATE DemoJpa t
          SET t.deleted = true
          WHERE t.id = :demoId
      """)
  void markAsDeleted(@Param("demoId") UUID value);
}

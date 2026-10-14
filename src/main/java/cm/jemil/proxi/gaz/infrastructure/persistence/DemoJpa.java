/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "t_demo")
@SQLRestriction("c_deleted = false")
class DemoJpa {

  @Id
  @Column(name = "c_id")
  private UUID id;

  @Column(name = "c_name")
  private String name;

  @Column(name = "c_description")
  private String description;

  @Column(name = "c_reference")
  private String reference;

  @Column(name = "c_deleted")
  private boolean deleted;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DemoJpa demoJpa = (DemoJpa) o;
    return Objects.equals(id, demoJpa.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}

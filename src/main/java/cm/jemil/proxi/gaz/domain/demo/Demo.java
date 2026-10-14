/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Demo {

  private final DemoId id;
  private final DemoReference reference;
  private DemoName name;
  private DemoDescription description;

  Demo(DemoId id, DemoName name, DemoReference reference, DemoDescription description) {
    this.id = id;
    this.name = name;
    this.reference = reference;
    this.description = description;
  }

  public static Demo of(
      DemoId id, DemoName name, DemoReference reference, DemoDescription description) {
    return new Demo(id, name, reference, description);
  }

  public static Demo reconstitute(
      DemoId id, DemoName name, DemoReference reference, DemoDescription description) {
    return new Demo(id, name, reference, description);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Demo demo)) {
      return false;
    }
    return Objects.equals(id, demo.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public UUID id() {
    return this.id.value();
  }

  public DemoCreatedEvent toCreatedEvent() {
    return new DemoCreatedEvent(this.id, this.name, this.reference, this.description);
  }

  public DemoUpdatedEvent update(DemoName name, DemoDescription description) {
    this.name = name;
    this.description = description;
    return new DemoUpdatedEvent(this.id, this.name, this.description);
  }
}

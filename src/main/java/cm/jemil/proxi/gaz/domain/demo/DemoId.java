package cm.jemil.proxi.gaz.domain.demo;

import java.util.UUID;

public record DemoId(UUID value) {

  public static DemoId generate() {
    return new DemoId(UUID.randomUUID());
  }

  public static DemoId from(UUID value) {
    return new DemoId(value);
  }
}

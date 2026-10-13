package cm.jemil.proxi.gaz.domain.demo;

public record DemoDescription(String value) {

  public static final DemoDescription NULL = new DemoDescription("-");

  public static DemoDescription from(String value) {
    return new DemoDescription(value);
  }
}

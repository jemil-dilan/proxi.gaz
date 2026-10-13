package cm.jemil.proxi.gaz.domain.demo;

public record DemoName(String value) {

  public static DemoName from(String value) {
    return new DemoName(value);
  }
}

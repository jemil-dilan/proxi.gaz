package cm.jemil.proxi.gaz.domain.demo;

public record DemoCreatedEvent(
    DemoId id, DemoName name, DemoReference reference, DemoDescription description) {}

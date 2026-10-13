package cm.jemil.proxi.gaz;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Tag("data")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}

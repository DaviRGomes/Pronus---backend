package com.inatel.prototipo_ia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = com.inatel.prototipo_ia.PrototipoIaApplication.class,
    properties = {
        "spring.jpa.hibernate.ddl-auto=none"
    })
public class PrototipoIaApplicationTests {

	@Test
	void contextLoads() {
	}

}

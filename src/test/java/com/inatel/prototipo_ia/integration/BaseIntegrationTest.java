package com.inatel.prototipo_ia.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
public abstract class BaseIntegrationTest {
    // Todos os testes de integração herdam essas configurações
}

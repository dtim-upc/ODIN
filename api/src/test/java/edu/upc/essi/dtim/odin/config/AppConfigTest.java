package edu.upc.essi.dtim.odin.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class AppConfigTest {

    @Autowired
    private AppConfig config;

    @Test
    void testGetDBTypeProperty() {
        Assertions.assertEquals("JENA", config.getDBTypeProperty());
    }

    @Test
    void testGetDiskPath() {
        Assertions.assertEquals("..\\api\\dbFiles\\diskFiles", config.getDiskPath());
    }

    @Test
    void testGetJenaPath() {
        Assertions.assertEquals("..\\api\\dbFiles\\jenaFiles", config.getJenaPath());
    }
}

package edu.upc.essi.dtim.odin.NextiaStore.GraphStore;

import edu.upc.essi.dtim.odin.config.AppConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GraphStoreFactoryTest {

    @Mock
    private AppConfig appConfig;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetInstance_Jena() {
        // Call the getInstance() method
        try {
            // Mock the AppConfig to return "JENA" as the DB type property
            when(appConfig.getDBTypeProperty()).thenReturn("JENA");

            GraphStoreInterface instance = GraphStoreFactory.getInstance(appConfig);

            // Assert that the instance is not null and is of type GraphStoreJenaImpl
            assertNotNull(instance);
            assertTrue(instance instanceof GraphStoreJenaImpl);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetInstance_UnknownDBType() throws Exception {
        // Mock the AppConfig to return an unknown DB type property
        when(appConfig.getDBTypeProperty()).thenReturn("UnknownDB");

        // Call the getInstance() method
        Assertions.assertThrows(Exception.class, () -> GraphStoreFactory.getInstance(appConfig));
    }

    @Test
    void testGetInstance_NullAppCOnfig() {
        GraphStoreInterface instance = null;
        Exception exception = null;
        // Test with null appConfig
        AppConfig nullAppConfig = null;

            try {
            instance = GraphStoreFactory.getInstance(nullAppConfig);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception instanceof IllegalArgumentException);
        assertEquals("appConfig cannot be null", exception.getMessage());
        assertNull(instance);
    }
}

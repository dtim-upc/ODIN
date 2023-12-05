package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatasetTest {

    @Test
    public void testDatasetConstructor() {
        Dataset dataset = new Dataset("id", "name", "description");
        assertNotNull(dataset.getCreated_at());
        assertEquals("name", dataset.getDatasetName());
        assertEquals("description", dataset.getDatasetDescription());
    }
}


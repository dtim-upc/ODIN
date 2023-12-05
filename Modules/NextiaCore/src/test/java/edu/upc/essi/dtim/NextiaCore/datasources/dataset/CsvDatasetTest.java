package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvDatasetTest {

    private final String TEST_FILE = "..\\NextiaCore\\src\\test\\java\\resources\\csvTestFile.csv".replace("\\", "/");
    private final String TEST_ID = "test_id";
    private final String TEST_NAME = "test_name";
    private final String TEST_DESC = "test_desc";

    @Test
    public void testConstructor() {
        CsvDataset dataset = new CsvDataset(TEST_ID, TEST_NAME, TEST_DESC, TEST_FILE);

        assertEquals(TEST_NAME, dataset.getDatasetName());
        assertEquals(TEST_DESC, dataset.getDatasetDescription());
        assertEquals(TEST_FILE, dataset.getPath());
    }

    @Test
    public void testConstructorExceptionJsonFile() {
        String WRONG_FORMAT_FILE_JSON = "..\\NextiaCore\\src\\test\\java\\resources\\jsonTestFile.json".replace("\\", "/");
        // Act
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> new CsvDataset(TEST_ID, TEST_NAME, TEST_DESC, WRONG_FORMAT_FILE_JSON));

        // Assert
        String expectedMessage = "Invalid file format. Only CSV files are supported.";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testConstructorExceptionTxtFile() {
        String WRONG_FORMAT_FILE_TXT = "..\\NextiaCore\\src\\test\\java\\resources\\test.txt".replace("\\", "/");
        // Act
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> new CsvDataset(TEST_ID, TEST_NAME, TEST_DESC, WRONG_FORMAT_FILE_TXT));

        // Assert
        String expectedMessage = "Invalid file format. Only CSV files are supported.";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCsvDatasetConstructorValidExtension() {
        assertDoesNotThrow(() -> new CsvDataset("id", "name", "description", "file.csv"));
    }

    @Test
    public void testCsvDatasetConstructorInvalidExtension() {
        assertThrows(IllegalArgumentException.class, () -> new CsvDataset("id", "name", "description", "file.txt"));
    }
}
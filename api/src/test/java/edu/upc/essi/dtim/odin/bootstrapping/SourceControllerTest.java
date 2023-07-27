package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;


class SourceControllerTest {
    @Mock
    private SourceService sourceService;

    @InjectMocks
    private SourceController sourceController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sourceService = Mockito.mock(SourceService.class);
        sourceController = new SourceController(sourceService);
    }

    @Test
    void testBootstrap_Success() {
        // Mock input parameters
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Test Description";
        MultipartFile attach_file = mock(MultipartFile.class);

        // Mock SourceService methods
        String filePath = "api/src/test/resources/csvTestFile.csv";
        Dataset extractedData = mock(Dataset.class);
        Dataset savedDataset = mock(Dataset.class);
        String visualSchema = "visual schema";
        Graph savedGraph = mock(LocalGraph.class);

        when(sourceService.reconstructFile(attach_file)).thenReturn(filePath);
        when(sourceService.extractData(filePath, datasetName, datasetDescription)).thenReturn(extractedData);
        when(sourceService.saveDataset(extractedData)).thenReturn(savedDataset);
        when(sourceService.transformToGraph(savedDataset)).thenReturn(graph);
        when(sourceService.generateVisualSchema(graph)).thenReturn(visualSchema);
        when(graph.getGraph()).thenReturn(savedGraph);
        when(sourceService.saveGraphToDatabase(graph)).thenReturn(true);

        // Perform the bootstrap operation
        ResponseEntity<?> response = sourceController.bootstrap(projectId, datasetName, datasetDescription, attach_file);

        // Verify the expected interactions and assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedDataset, response.getBody());
        verify(sourceService).reconstructFile(attach_file);
        verify(sourceService).extractData(filePath, datasetName, datasetDescription);
        verify(sourceService).saveDataset(extractedData);
        verify(sourceService).transformToGraph(savedDataset);
        verify(sourceService).generateVisualSchema(graph);
        verify(sourceService).saveGraphToDatabase(graph);
        verify(sourceService).addDatasetIdToProject(projectId, savedDataset);
        verifyNoMoreInteractions(sourceService);
    }

    @Test
    void testBootstrap_UnsupportedOperationException() {
        // Mock input parameters
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Test Description";
        MultipartFile attach_file = mock(MultipartFile.class);

        // Mock SourceService method
        when(sourceService.reconstructFile(attach_file)).thenThrow(new UnsupportedOperationException());

        // Perform the bootstrap operation and verify the expected exception
        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> sourceController.bootstrap(projectId, datasetName, datasetDescription, attach_file));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Data source not created successfully", exception.getReason());

        verify(sourceService).reconstructFile(attach_file);
        verifyNoMoreInteractions(sourceService);
    }

    @Test
    void testBootstrap_Exception() {
        // Mock input parameters
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Test Description";
        MultipartFile attach_file = mock(MultipartFile.class);

        // Mock SourceService method
        when(sourceService.reconstructFile(attach_file)).thenThrow(new RuntimeException());

        // Perform the bootstrap operation and verify the expected exception
        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> sourceController.bootstrap(projectId, datasetName, datasetDescription, attach_file));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("An error occurred while creating the data source", exception.getReason());

        verify(sourceService).reconstructFile(attach_file);
        verifyNoMoreInteractions(sourceService);
    }

    @Test
    void testSavingDatasetObject_SuccessCsv() {
        // Mock the dependencies
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Description";
        String path = "../api/src/test/resources/csvTestFile.csv";
        Dataset savedDataset = Mockito.mock(Dataset.class);

        Mockito.when(sourceService.saveDataset(Mockito.any())).thenReturn(savedDataset);
        Mockito.doNothing().when(sourceService).addDatasetIdToProject(projectId, savedDataset);

        // Call the savingDatasetObject method
        ResponseEntity<Dataset> response = sourceController.savingDatasetObject(datasetName, datasetDescription, path, projectId);

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(savedDataset, response.getBody());
    }

    @Test
    void testSavingDatasetObject_SuccessJson() {
        // Mock the dependencies
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Description";
        String path = "../api/src/test/resources/jsonTestFile.json";
        Dataset savedDataset = Mockito.mock(Dataset.class);

        Mockito.when(sourceService.saveDataset(Mockito.any())).thenReturn(savedDataset);
        Mockito.doNothing().when(sourceService).addDatasetIdToProject(projectId, savedDataset);

        // Call the savingDatasetObject method
        ResponseEntity<Dataset> response = sourceController.savingDatasetObject(datasetName, datasetDescription, path, projectId);

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(savedDataset, response.getBody());
    }

    @Test
    void testSavingDatasetObject_UnsupportedOperationException() {
        // Mock the dependencies
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Description";
        String path = "file/path";

        // Call the savingDatasetObject method and verify the exception
        Assertions.assertThrows(ResponseStatusException.class, () ->
                sourceController.savingDatasetObject(datasetName, datasetDescription, path, projectId));
    }

    @Test
    void testSavingDatasetObject_Exception() {
        // Mock the dependencies
        String projectId = "123";
        String datasetName = "Test Dataset";
        String datasetDescription = "Description";
        String path = "file/path";

        Mockito.when(sourceService.saveDataset(Mockito.any())).thenThrow(new RuntimeException("Error"));

        // Call the savingDatasetObject method and verify the exception
        Assertions.assertThrows(ResponseStatusException.class, () ->
                sourceController.savingDatasetObject(datasetName, datasetDescription, path, projectId));
    }

    @Test
    void testDeleteDatasource_Success() {
        // Mock the dependencies
        String projectId = "123";
        String datasourceId = "456";

        Mockito.when(sourceService.projectContains(projectId, datasourceId)).thenReturn(true);
        Mockito.doNothing().when(sourceService).deleteDatasetFromProject(projectId, datasourceId);

        // Call the deleteDatasource method
        ResponseEntity<Boolean> response = sourceController.deleteDatasource(projectId, datasourceId);

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    void testDeleteDatasource_NotFound() {
        // Mock the dependencies
        String projectId = "123";
        String datasourceId = "456";

        Mockito.when(sourceService.projectContains(projectId, datasourceId)).thenReturn(false);

        // Call the deleteDatasource method and verify the response
        ResponseEntity<Boolean> response = sourceController.deleteDatasource(projectId, datasourceId);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void testGetDatasourcesFromProject_Success() {
        // Mock the dependencies
        String projectId = "123";
        List<Dataset> datasets = List.of(Mockito.mock(Dataset.class), Mockito.mock(Dataset.class));

        Mockito.when(sourceService.getDatasetsOfProject(projectId)).thenReturn(datasets);

        // Call the getDatasourcesFromProject method
        ResponseEntity<Object> response = sourceController.getDatasourcesFromProject(projectId);

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(datasets, response.getBody());
    }
    @Test
    void testGetDatasourcesFromProject_NotFound() {
        // Mock the dependencies
        String projectId = "123";

        Mockito.when(sourceService.getDatasetsOfProject(projectId)).thenReturn(new ArrayList<>());

        // Call the getDatasourcesFromProject method and verify the response
        ResponseEntity<Object> response = sourceController.getDatasourcesFromProject(projectId);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("No datasets found",response.getBody());
    }

    @Test
    void testGetDatasourcesFromProject_InternalError() {
        // Mock the dependencies
        String projectId = "123";

        Mockito.when(sourceService.getDatasetsOfProject(projectId)).thenReturn(null);

        // Call the getDatasourcesFromProject method and verify the response
        ResponseEntity<Object> response = sourceController.getDatasourcesFromProject(projectId);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertEquals("An error occurred",response.getBody());
    }

    @Test
    void testGetAllDatasource() {
        // Mock the behavior of the sourceService
        List<Dataset> mockDatasets = new ArrayList<>();
        mockDatasets.add(new Dataset());
        mockDatasets.add(new Dataset());
        when(sourceService.getDatasets()).thenReturn(mockDatasets);

        // Test when datasets are found
        ResponseEntity<Object> response = sourceController.getAllDatasource();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDatasets, response.getBody());

        // Test when no datasets are found
        mockDatasets.clear();
        response = sourceController.getAllDatasource();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No datasets found", response.getBody());

        // Test when an exception occurs
        when(sourceService.getDatasets()).thenAnswer(invocation -> {
            throw new Exception("Some exception");
        });
        response = sourceController.getAllDatasource();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
    }


}

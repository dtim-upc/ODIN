package edu.upc.essi.dtim.odin.datasets;

public class SourceControllerTest {
//
//    private SourceController sourceController;
//    private SourceService sourceService;
//
//    @BeforeEach
//    public void setUp() {
//        sourceService = mock(SourceService.class);
//        sourceController = new SourceController(sourceService);
//    }
//
//    @Test
//    public void testBootstrap() {
//        // Mock data for the test
//        String projectId = "project123";
//        String repositoryId = "repo123";
//        String repositoryName = "Repo Name";
//        String datasetName = "Dataset Name";
//        String datasetDescription = "Dataset Description";
//
//        Boolean vbooltrue = true;
//
//        // Create a list of mock MultipartFile objects
//        List<MultipartFile> attachFiles = new ArrayList<>();
//        MultipartFile mockFile1 = mock(MultipartFile.class);
//        attachFiles.add(mockFile1);
//
//        // Mock behavior of sourceService methods
//        when(sourceService.reconstructFile(any(MultipartFile.class))).thenReturn("mockedFilePath");
//        when(sourceService.extractData(anyString(), anyString(), anyString())).thenReturn(new Dataset());
//        when(sourceService.saveDataset(any(Dataset.class))).thenReturn(new Dataset());
//        when(sourceService.bootstrapDataset(any(Dataset.class))).thenReturn(CoreGraphFactory.createLocalGraph());
//        when(sourceService.generateVisualSchema(any(Graph.class))).thenReturn("Mocked Visual Schema");
//        when(sourceService.setLocalGraphToDataset(any(Dataset.class), any(Graph.class))).thenReturn(new Dataset());
//        when(sourceService.findRepositoryById(anyString())).thenReturn(new DataRepository());
//        when(sourceService.addDatasetToRepository(anyString(), anyString())).thenReturn(new DataRepository());
//        when(sourceService.projectHasIntegratedGraph(anyString())).thenReturn(false);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.bootstrap(projectId, repositoryId, repositoryName, datasetName, datasetDescription, attachFiles);
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // You can add more assertions to check the expected behavior of the controller
//    }
//
//    @Test
//    public void testDeleteDatasource() {
//        // Mock data for the test
//        String projectId = "project123";
//        String datasetId = "dataset123";
//
//        // Mock behavior of sourceService method
//        when(sourceService.projectContains(anyString(), anyString())).thenReturn(true);
//        // Use doNothing() for a void method
//        doNothing().when(sourceService).deleteDatasetFromProject(anyString(), anyString());
//
//        // Call the controller method
//        ResponseEntity<Boolean> response = sourceController.deleteDatasource(projectId, datasetId);
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response body is true
//        assertEquals(true, response.getBody());
//    }
//
//    @Test
//    public void testGetDatasourcesFromProjectWithDatasets() {
//        // Mock data for the test
//        String projectId = "project123";
//        List<Dataset> datasets = new ArrayList<>();
//        datasets.add(new Dataset("dataset1", "Dataset 1", "Description 1"));
//        datasets.add(new Dataset("dataset2", "Dataset 2", "Description 2"));
//
//        // Mock behavior of sourceService method
//        when(sourceService.getDatasetsOfProject(projectId)).thenReturn(datasets);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getDatasourcesFromProject(projectId);
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response body matches the expected datasets
//        assertEquals(datasets, response.getBody());
//    }
//
//    @Test
//    public void testGetDatasourcesFromProjectWithNoDatasets() {
//        // Mock data for the test
//        String projectId = "project123";
//        List<Dataset> datasets = new ArrayList<>();
//
//        // Mock behavior of sourceService method
//        when(sourceService.getDatasetsOfProject(projectId)).thenReturn(datasets);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getDatasourcesFromProject(projectId);
//
//        // Verify that the response status is NO_CONTENT (204)
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//
//        // Verify that the response body contains the expected message
//        assertEquals("There are no datasets yet", response.getBody());
//    }
//
//    @Test
//    public void testGetDatasourcesFromProjectWithException() {
//        // Mock data for the test
//        String projectId = "project123";
//
//        // Mock behavior of sourceService method to throw an exception
//        when(sourceService.getDatasetsOfProject(projectId)).thenThrow(new RuntimeException("Error"));
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getDatasourcesFromProject(projectId);
//
//        // Verify that the response status is INTERNAL_SERVER_ERROR (500)
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//
//        // Verify that the response body contains the expected error message
//        assertEquals("An error occurred", response.getBody());
//    }
//
//    @Test
//    public void testGetRepositoriesFromProjectWithRepositories() {
//        // Mock data for the test
//        String projectId = "project123";
//        List<DataRepository> repositories = new ArrayList<>();
//        repositories.add(new DataRepository());
//        repositories.add(new DataRepository());
//
//        // Mock behavior of sourceService method
//        when(sourceService.getRepositoriesOfProject(projectId)).thenReturn(repositories);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getRepositoriesFromProject(projectId);
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response body matches the expected repositories
//        assertEquals(repositories, response.getBody());
//    }
//
//    @Test
//    public void testGetRepositoriesFromProjectWithNoRepositories() {
//        // Mock data for the test
//        String projectId = "project123";
//        List<DataRepository> repositories = new ArrayList<>();
//
//        // Mock behavior of sourceService method
//        when(sourceService.getRepositoriesOfProject(projectId)).thenReturn(repositories);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getRepositoriesFromProject(projectId);
//
//        // Verify that the response status is NO_CONTENT (204)
//        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//
//        // Verify that the response body contains the expected message
//        assertEquals("There are no repositories yet", response.getBody());
//    }
//
//    @Test
//    public void testGetRepositoriesFromProjectWithException() {
//        // Mock data for the test
//        String projectId = "project123";
//
//        // Mock behavior of sourceService method to throw an exception
//        when(sourceService.getRepositoriesOfProject(projectId)).thenThrow(new RuntimeException("Error"));
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getRepositoriesFromProject(projectId);
//
//        // Verify that the response status is INTERNAL_SERVER_ERROR (500)
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//
//        // Verify that the response body contains the expected error message
//        assertEquals("An error occurred", response.getBody());
//    }
//
//    @Test
//    public void testGetAllDatasourceWithDatasets() {
//        // Mock data for the test
//        List<Dataset> datasets = new ArrayList<>();
//        datasets.add(new Dataset());
//        datasets.add(new Dataset());
//
//        // Mock behavior of sourceService method
//        when(sourceService.getDatasets()).thenReturn(datasets);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getAllDatasource();
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response body matches the expected datasets
//        assertEquals(datasets, response.getBody());
//    }
//
//    @Test
//    public void testGetAllDatasourceWithNoDatasets() {
//        // Mock data for the test
//        List<Dataset> datasets = new ArrayList<>();
//
//        // Mock behavior of sourceService method
//        when(sourceService.getDatasets()).thenReturn(datasets);
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getAllDatasource();
//
//        // Verify that the response status is NOT_FOUND (404)
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//        // Verify that the response body contains the expected message
//        assertEquals("No datasets found", response.getBody());
//    }
//
//    @Test
//    public void testGetAllDatasourceWithException() {
//        // Mock behavior of sourceService method to throw an exception
//        when(sourceService.getDatasets()).thenThrow(new RuntimeException("Error"));
//
//        // Call the controller method
//        ResponseEntity<Object> response = sourceController.getAllDatasource();
//
//        // Verify that the response status is INTERNAL_SERVER_ERROR (500)
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
//
//        // Verify that the response body contains the expected error message
//        assertEquals("An error occurred", response.getBody());
//    }
//
//    @Test
//    public void testEditDataset() {
//        // Mock data for the test
//        String projectId = "project1";
//        String datasetId = "dataset1";
//        String datasetName = "New Dataset Name";
//        String datasetDescription = "New Dataset Description";
//        String repositoryId = "repository1";
//        String repositoryName = "Repository Name";
//
//        // Mock behavior of sourceService method to return true (dataset edited successfully)
//        when(sourceService.editDataset(any(Dataset.class))).thenReturn(true);
//
//        // Call the controller method
//        ResponseEntity<Boolean> response = sourceController.editDataset(
//                projectId,
//                datasetId,
//                datasetName,
//                datasetDescription,
//                repositoryId,
//                repositoryName
//        );
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response body is true (indicating successful edit)
//        assertTrue(response.getBody());
//    }
//
//    @Test
//    public void testEditDatasetWithRepositoryCreation() {
//        // Mock data for the test
//        String projectId = "project1";
//        String datasetId = "dataset1";
//        String datasetName = "New Dataset Name";
//        String datasetDescription = "New Dataset Description";
//        String repositoryName = "Repository Name";
//
//        // Mock behavior of sourceService method to return true (dataset edited successfully)
//        when(sourceService.editDataset(any(Dataset.class))).thenReturn(true);
//        // Mock behavior of sourceService method to create a new repository
//        when(sourceService.createRepository(repositoryName)).thenReturn(new DataRepository());
//
//        // Call the controller method with an empty repositoryId (indicating repository creation)
//        ResponseEntity<Boolean> response = sourceController.editDataset(
//                projectId,
//                datasetId,
//                datasetName,
//                datasetDescription,
//                "", // Empty repositoryId to indicate repository creation
//                repositoryName
//        );
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response body is true (indicating successful edit)
//        assertTrue(response.getBody());
//    }
//
//    @Test
//    public void testEditDatasetWithDatasetNotFound() {
//        // Mock data for the test
//        String projectId = "project1";
//        String datasetId = "nonexistentDataset";
//        String datasetName = "New Dataset Name";
//        String datasetDescription = "New Dataset Description";
//        String repositoryId = "repository1";
//        String repositoryName = "Repository Name";
//
//        // Mock behavior of sourceService method to return false (dataset not found)
//        when(sourceService.editDataset(any(Dataset.class))).thenReturn(false);
//
//        // Call the controller method
//        ResponseEntity<Boolean> response = sourceController.editDataset(
//                projectId,
//                datasetId,
//                datasetName,
//                datasetDescription,
//                repositoryId,
//                repositoryName
//        );
//
//        // Verify that the response status is NOT_FOUND (404)
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//        // Verify that the response body is null
//        assertNull(response.getBody());
//    }
//
//    @Test
//    public void testDownloadDatasetSchema() {
//        // Mock data for the test
//        String projectId = "project1";
//        String datasetId = "dataset1";
//
//        // Mock behavior of sourceService method to return a dataset
//        Dataset dataset = new Dataset(datasetId, "Dataset Name", "Description test");
//        when(sourceService.getDatasetById(datasetId)).thenReturn(dataset);
//
//        // Call the controller method
//        ResponseEntity<InputStreamResource> response = sourceController.downloadDatasetSchema(
//                projectId,
//                datasetId
//        );
//
//        // Verify that the response status is OK (200)
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//        // Verify that the response contains the Turtle schema file
//        assertNotNull(response.getBody());
//
//        // Verify that the headers indicate a file attachment
//        assertTrue(response.getHeaders().containsKey("Content-Disposition"));
//        assertEquals("attachment; filename=Dataset Name.ttl", response.getHeaders().getFirst("Content-Disposition"));
//    }
//
//    @Test
//    public void testDownloadDatasetSchemaNotFound() {
//        // Mock data for the test
//        String projectId = "project1";
//        String datasetId = "nonexistentDataset";
//
//        // Mock behavior of sourceService method to return null (dataset not found)
//        when(sourceService.getDatasetById(datasetId)).thenReturn(null);
//
//        // Call the controller method for a nonexistent dataset
//        ResponseEntity<InputStreamResource> response = sourceController.downloadDatasetSchema(
//                projectId,
//                datasetId
//        );
//
//        // Verify that the response status is NOT_FOUND (404)
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//
//        // Verify that the response body is null
//        assertNull(response.getBody());
//    }
}
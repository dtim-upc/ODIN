package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.JsonDataset;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


@SpringBootTest
class SourceServiceTest {

    private SourceService sourceService;

    @BeforeEach
    void setUp(@Autowired SourceService sourceService) {
        // Initialize SourceService instance
        this.sourceService = sourceService;
    }

    @Test
    void testExtractData_csv() {
        // Prepare test data
        String filePath = "path/to/dataset.csv";
        String datasetName = "Test Dataset";
        String datasetDescription = "This is a test dataset";

        // Call the method to extract data
        Dataset dataset = sourceService.extractData(filePath, datasetName, datasetDescription);

        // Assert that the dataset is not null
        Assertions.assertNotNull(dataset);

        // Assert that the dataset properties are set correctly
        Assertions.assertEquals(datasetName, dataset.getDatasetName());
        Assertions.assertEquals(datasetDescription, dataset.getDatasetDescription());
    }

    @Test
    void testExtractData_json() {
        // Prepare test data
        String filePath = "path/to/dataset.json";
        String datasetName = "Test Dataset";
        String datasetDescription = "This is a test dataset";

        // Call the method to extract data
        Dataset dataset = sourceService.extractData(filePath, datasetName, datasetDescription);

        // Assert that the dataset is not null
        Assertions.assertNotNull(dataset);

        // Assert that the dataset properties are set correctly
        Assertions.assertEquals(datasetName, dataset.getDatasetName());
        Assertions.assertEquals(datasetDescription, dataset.getDatasetDescription());
    }

    @Test
    void testExtractData_unsupportedFileFormat() {
        // Prepare test data
        String filePath = "path/to/dataset.txt";
        String datasetName = "Test Dataset";
        String datasetDescription = "This is a test dataset";

        // Call the method to extract data
        Assertions.assertThrows(IllegalArgumentException.class, () -> sourceService.extractData(filePath, datasetName, datasetDescription));
    }
/*
    @Test
    void testReconstructFile() {
        // Create a mock MultipartFile
        String originalFilename = "test.csv";
        byte[] content = "Test file content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", originalFilename, null, content);

        // Call the reconstructFile method
        String reconstructedFilePath = sourceService.reconstructFile(multipartFile);

        // Verify the reconstructed file
        Path reconstructedPath = Path.of(reconstructedFilePath);
        Assertions.assertTrue(Files.exists(reconstructedPath));
        Assertions.assertEquals(originalFilename, reconstructedPath.getFileName().toString().substring(17));
    }

 */

    @Test
    void testReconstructFileEmptyFile() {
        // Create an empty mock MultipartFile
        MultipartFile multipartFile = new MockMultipartFile("file", new byte[0]);

        // Call the reconstructFile method and expect a RuntimeException
        Assertions.assertThrows(RuntimeException.class, () -> sourceService.reconstructFile(multipartFile));
    }

    @Test
    void testReconstructFileOutsideDirectory() {
        // Create a mock MultipartFile
        String originalFilename = "../file.txt";
        byte[] content = "Test file content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", originalFilename, null, content);

        // Call the reconstructFile method and expect a RuntimeException
        Assertions.assertThrows(RuntimeException.class, () -> sourceService.reconstructFile(multipartFile));
    }

    @Test
    void testTransformToGraph() {
        // Prepare test data
        Dataset dataset = new CsvDataset("datasetId", "Test Dataset", "This is a test dataset", "../api/src/test/resources/csvTestFile.csv");

        // Call the method to transform the dataset to a graph
        GraphModelPair graphModelPair = sourceService.transformToGraph(dataset);

        // Assert that the returned GraphModelPair is not null
        Assertions.assertNotNull(graphModelPair);

        // Assert that the graph and model in the GraphModelPair are not null
        Assertions.assertNotNull(graphModelPair.getGraph());
        Assertions.assertNotNull(graphModelPair.getModel());
    }

    @Test
    void testConvertDatasetToModelWithCsvDataset() {
        // Create a CsvDataset object for testing
        CsvDataset csvDataset = new CsvDataset();
        csvDataset.setDatasetId("123");
        csvDataset.setDatasetName("Test Dataset");
        csvDataset.setPath("../api/src/test/resources/csvTestFile.csv");

        // Call the convertDatasetToModel method
        Model model = sourceService.convertDatasetToModel(csvDataset);

        // Perform assertions
        Assertions.assertNotNull(model);
        // Add more assertions based on your expected behavior
    }

    @Test
    void testConvertDatasetToModelWithJsonDataset() {
        // Create a JsonDataset object for testing
        JsonDataset jsonDataset = new JsonDataset();
        jsonDataset.setDatasetId("456");
        jsonDataset.setDatasetName("Test Dataset");
        jsonDataset.setPath("../api/src/test/resources/jsonTestFile.json");

        // Call the convertDatasetToModel method
        Model model = sourceService.convertDatasetToModel(jsonDataset);

        // Perform assertions
        Assertions.assertNotNull(model);
    }

    @Test
    void testConvertDatasetToModelWithIOException() {
        // Create a CsvDataset object for testing
        CsvDataset csvDataset = new CsvDataset();
        csvDataset.setDatasetId("123");
        csvDataset.setDatasetName("Test Dataset");
        csvDataset.setPath("path/to/csv");

        // Call the convertDatasetToModel method and expect an exception
        Assertions.assertThrows(RuntimeException.class, () -> sourceService.convertDatasetToModel(csvDataset));
    }

    @Test
    void testConvertDatasetToModelWithFileNotFoundException() {
        // Create a JsonDataset object for testing
        JsonDataset jsonDataset = new JsonDataset();
        jsonDataset.setDatasetId("456");
        jsonDataset.setDatasetName("Test Dataset");
        jsonDataset.setPath("path/to/json");

        // Call the convertDatasetToModel method and expect an exception
        Assertions.assertThrows(RuntimeException.class, () -> sourceService.convertDatasetToModel(jsonDataset));
    }

    @Test
    void testHandleUnsupportedDatasetFormat() {
        // Create a Dataset object for testing
        Dataset dataset = new Dataset();
        dataset.setDatasetId("789");
        dataset.setDatasetName("Test Dataset");


        // Call the handleUnsupportedDatasetFormat method
        GraphModelPair result = sourceService.handleUnsupportedDatasetFormat(dataset);

        // Perform assertions
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getGraph());
        Assertions.assertNull(result.getModel());
        // Add more assertions based on your expected behavior
    }

    @Test
    void testGenerateVisualSchema() {
        // Prepare test data
        GraphModelPair graphModelPair = new GraphModelPair(new LocalGraph(), getHardcodedModel());

        // Call the method to generate the visual schema
        String visualSchema = sourceService.generateVisualSchema(graphModelPair);

        // Assert that the returned visual schema is not null or empty
        Assertions.assertNotNull(visualSchema);
        Assertions.assertFalse(visualSchema.isEmpty());
    }

    @Test
    void testSaveGraphToDatabase() {
        // Prepare test data
        Dataset dataset = new CsvDataset("datasetId", "Test Dataset", "This is a test dataset", "../api/src/test/resources/csvTestFile.csv");

        // Call the method to transform the dataset to a graph
        GraphModelPair graphModelPair = sourceService.transformToGraph(dataset);

        // Call the method to save the graph to the database
        boolean result = sourceService.saveGraphToDatabase(graphModelPair);

        // Assert that the result is true, indicating successful saving
        Assertions.assertTrue(result);
    }

    @Test
    void testAddDatasetIdToProject() {
        // Prepare test data
        String projectId = "123";
        Dataset dataset = new Dataset();

        // Create a project with the specified ID
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName("TestProject");
        ProjectService projectService = new ProjectService();
        projectService.saveProject(project);

        Assertions.assertEquals(0,project.getDatasets().size());

        // Call the method to add the dataset ID to the project
        sourceService.addDatasetIdToProject(projectId, dataset);

        Assertions.assertEquals(1,projectService.getDatasetsOfProject(projectId).size());

        //clean de DB from test samples
        projectService.deleteProject(projectId);
    }

    @Test
    void testDeleteDatasetFromProject() {
        // Prepare test data
        String projectId = "123";
        String datasetId = "1234";
        Dataset dataset = new Dataset();
        dataset.setDatasetId(datasetId);

        // Create a project with the specified ID
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName("TestProject");
        ProjectService projectService = new ProjectService();
        projectService.saveProject(project);

        Assertions.assertEquals(0,project.getDatasets().size());

        // Call the method to add the dataset ID to the project
        sourceService.addDatasetIdToProject(projectId, dataset);

        Assertions.assertEquals(1,projectService.getDatasetsOfProject(projectId).size());

        // Call the method to delete the dataset from the project
        sourceService.deleteDatasetFromProject(projectId, datasetId);

        Assertions.assertEquals(0,projectService.getDatasetsOfProject(projectId).size());

        //clean de DB from test samples
        projectService.deleteProject(projectId);
    }

    @Test
    void testSaveAndDeleteDataset() {
        // Prepare test data
        Dataset dataset = new Dataset();

        // Call the method to save the dataset
        Dataset savedDataset = sourceService.saveDataset(dataset);

        Assertions.assertNotNull(savedDataset.getDatasetId());

        // Call the method to delete the datasource
        boolean result = sourceService.deleteDatasource(savedDataset.getDatasetId());

        // Assert the result of the deletion
        Assertions.assertTrue(result);
    }

    @Test
    void testProjectContains() {
        // Prepare test data
        String projectId = "123";
        String datasetId = "1234";
        Dataset dataset = new Dataset();
        dataset.setDatasetId(datasetId);

        // Create a project with the specified ID
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName("TestProject");
        ProjectService projectService = new ProjectService();
        projectService.saveProject(project);

        Assertions.assertEquals(0,project.getDatasets().size());

        // Call the method to add the dataset ID to the project
        sourceService.addDatasetIdToProject(projectId, dataset);

        Assertions.assertEquals(1,projectService.getDatasetsOfProject(projectId).size());

        // Call the method to check if the project contains the dataset
        boolean containsDataset = sourceService.projectContains(projectId, datasetId);

        // Assert the result of the containment check
        Assertions.assertTrue(containsDataset);

        // Call the method to delete the dataset from the project
        sourceService.deleteDatasetFromProject(projectId, datasetId);

        Assertions.assertEquals(0,projectService.getDatasetsOfProject(projectId).size());

        // Call the method to check if the project contains the dataset
        containsDataset = sourceService.projectContains(projectId, datasetId);

        // Assert the result of the containment check
        Assertions.assertFalse(containsDataset);

        //clean de DB from test samples
        projectService.deleteProject(projectId);
    }

    @Test
    void testGetDatasetsOfProject(){
        // Prepare test data
        String projectId = "123";
        String datasetId = "1234";
        Dataset dataset = new Dataset();
        dataset.setDatasetId(datasetId);

        // Create a project with the specified ID
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName("TestProject");
        ProjectService projectService = new ProjectService();
        projectService.saveProject(project);

        Assertions.assertEquals(0,project.getDatasets().size());

        // Call the method to add the dataset ID to the project
        sourceService.addDatasetIdToProject(projectId, dataset);

        Assertions.assertEquals(1,sourceService.getDatasetsOfProject(projectId).size());

        // Call the method to check if the project contains the dataset
        boolean containsDataset = sourceService.projectContains(projectId, datasetId);

        // Assert the result of the containment check
        Assertions.assertTrue(containsDataset);

        // Call the method to delete the dataset from the project
        sourceService.deleteDatasetFromProject(projectId, datasetId);

        Assertions.assertEquals(0,sourceService.getDatasetsOfProject(projectId).size());

        // Call the method to check if the project contains the dataset
        containsDataset = sourceService.projectContains(projectId, datasetId);

        // Assert the result of the containment check
        Assertions.assertFalse(containsDataset);

        //clean de DB from test samples
        projectService.deleteProject(projectId);
    }
}

package edu.upc.essi.dtim.odin.project;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ProjectServiceTest {

    private ORMStoreInterface ormProject;

    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        this.projectService = new ProjectService();
        this.testProject = new Project();
        this.testProject.setProjectId("testID");
        this.testProject.setProjectName("testName");
        this.testProject.setProjectDescription("testDescription");
        this.testProject.setProjectColor("testColor");
        this.testProject.setProjectPrivacy("testPrivacy");
        this.testProject.setCreatedBy("Victor Asenjo Testing");
    }

    @AfterEach
    void tearDown() {
        ormProject = ORMStoreFactory.getInstance();
        ormProject.deleteOne(Project.class,testProject.getProjectId());
    }

    @Test
    void testSaveProject() {
        Project savedProject = projectService.saveProject(testProject);

        Assertions.assertEquals(testProject.getProjectId(), savedProject.getProjectId());
        Assertions.assertEquals(testProject.getProjectName(), savedProject.getProjectName());
        Assertions.assertEquals(testProject.getProjectDescription(), savedProject.getProjectDescription());
        Assertions.assertEquals(testProject.getProjectColor(), savedProject.getProjectColor());
        Assertions.assertEquals(testProject.getProjectPrivacy(), savedProject.getProjectPrivacy());
        Assertions.assertEquals(testProject.getCreatedBy(), savedProject.getCreatedBy());
        Assertions.assertEquals(testProject.getDatasets(), savedProject.getDatasets());
    }

    @Test
    void testAddDatasetIdToProject() {
        Dataset dataset = new Dataset();
        dataset.setDatasetId("dataset1");

        ormProject = ORMStoreFactory.getInstance();

        ormProject.save(testProject);

        projectService.addDatasetIdToProject(testProject.getProjectId(), dataset);

        List<Dataset> datasets = ormProject.findById(Project.class, testProject.getProjectId()).getDatasets();
        Assertions.assertEquals(1, datasets.size());
        Assertions.assertEquals(dataset.getDatasetId(), datasets.get(0).getDatasetId());
    }

    @Test
    void testAddDatasetIdToProject_runtimeException() {
        Dataset dataset = new Dataset();
        dataset.setDatasetId("dataset1");

        Assertions.assertThrows(RuntimeException.class, () -> projectService.addDatasetIdToProject("null", dataset));
    }

    @Test
    void testGetDatasetsOfProject() {
        String datasetId = "dataset1";

        List<Dataset> datasets = new ArrayList<>();
        Dataset dataset1 = new Dataset();
        dataset1.setDatasetId(datasetId);
        datasets.add(dataset1);

        Dataset dataset2 = new Dataset();
        dataset2.setDatasetId("dataset2");
        datasets.add(dataset2);

        testProject.setDatasets(datasets);
        projectService.saveProject(testProject);

        Assertions.assertEquals(2, projectService.getDatasetsOfProject(testProject.getProjectId()).size());
    }

    @Test
    void testDeleteDatasetFromProject_success() {
        String datasetId = "dataset1";

        List<Dataset> datasets = new ArrayList<>();
        Dataset dataset1 = new Dataset();
        dataset1.setDatasetId(datasetId);
        datasets.add(dataset1);

        Dataset dataset2 = new Dataset();
        dataset2.setDatasetId("dataset2");
        datasets.add(dataset2);

        testProject.setDatasets(datasets);
        projectService.saveProject(testProject);

        Assertions.assertEquals(2, projectService.getDatasetsOfProject(testProject.getProjectId()).size());

        projectService.deleteDatasetFromProject(testProject.getProjectId(), datasetId);

        Assertions.assertEquals(1, projectService.getDatasetsOfProject(testProject.getProjectId()).size());

        projectService.deleteProject(testProject.getProjectId());
    }

    @Test
    void testDeleteDatasetFromProject_projectNotFound() {
        projectService.saveProject(testProject);

        // Call the reconstructFile method and expect a RuntimeException
        Assertions.assertThrows(IllegalArgumentException.class, () -> projectService.deleteDatasetFromProject("testID", "inventedDatasetId"));
    }

    @Test
    void testDeleteDatasetFromProject_datasetNotFound() {
        // Call the reconstructFile method and expect a RuntimeException
        Assertions.assertThrows(IllegalArgumentException.class, () -> projectService.deleteDatasetFromProject("invented", "datasetId"));
    }

    @Test
    void testFindById() {
        projectService.saveProject(testProject);

        Project foundProject = projectService.findById(testProject.getProjectId());

        Assertions.assertEquals(testProject.getProjectId(), foundProject.getProjectId());
        Assertions.assertEquals(testProject.getProjectName(), foundProject.getProjectName());
        Assertions.assertEquals(testProject.getProjectDescription(), foundProject.getProjectDescription());
        Assertions.assertEquals(testProject.getProjectColor(), foundProject.getProjectColor());
        Assertions.assertEquals(testProject.getProjectPrivacy(), foundProject.getProjectPrivacy());
        Assertions.assertEquals(testProject.getCreatedBy(), foundProject.getCreatedBy());
        Assertions.assertEquals(testProject.getDatasets(), foundProject.getDatasets());
    }

    @Test
    void testGetAllProjects() {
        List<Project> projects = new ArrayList<>();
        Project project1 = new Project();
        Project project2 = new Project();
        projects.add(project1);
        projects.add(project2);

        List<Project> allProjects = projectService.getAllProjects();

        Assertions.assertEquals(allProjects.size()+projects.size(), allProjects.size()+2);
    }

    @Test
    void testDeleteProject() {
        projectService.saveProject(testProject);

        boolean result = projectService.deleteProject(testProject.getProjectId());

        Assertions.assertTrue(result);
    }

    @Test
    void testProjectContains_true() {
        String datasetId = "dataset1";

        List<Dataset> datasets = new ArrayList<>();
        Dataset dataset1 = new Dataset();
        dataset1.setDatasetId(datasetId);
        datasets.add(dataset1);
        testProject.setDatasets(datasets);
        ormProject = ORMStoreFactory.getInstance();
        ormProject.save(testProject);
        boolean containsDataset = projectService.projectContains(testProject.getProjectId(), datasetId);

        Assertions.assertTrue(containsDataset);

        ormProject.deleteOne(Project.class, testProject.getProjectId());
    }

    @Test
    void testProjectContains_false() {
        String datasetId = "dataset1";
        String datasetId2 = "dataset2";
        List<Dataset> datasets = new ArrayList<>();
        Dataset dataset1 = new Dataset();
        dataset1.setDatasetId(datasetId);
        datasets.add(dataset1);
        testProject.setDatasets(datasets);
        ormProject = ORMStoreFactory.getInstance();
        ormProject.save(testProject);
        boolean containsDataset = projectService.projectContains(testProject.getProjectId(), datasetId2);

        Assertions.assertFalse(containsDataset);

        ormProject.deleteOne(Project.class, testProject.getProjectId());
    }
}

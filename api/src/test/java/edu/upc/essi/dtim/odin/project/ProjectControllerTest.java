package edu.upc.essi.dtim.odin.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveProject() {
        // Create a sample project
        Project project = new Project();
        project.setProjectId("testProject");
        project.setProjectName("Test Project");

        // Mock the projectService.saveProject method
        Mockito.when(projectService.saveProject(project)).thenReturn(project);

        // Call the saveProject method
        ResponseEntity<Project> response = projectController.saveProject(project);

        // Verify the response
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(project, response.getBody());

        // Verify that projectService.saveProject was called with the correct project
        Mockito.verify(projectService).saveProject(project);
    }

    @Test
    void testGetProject_ExistingProject() {
        // Create a sample project
        Project project = new Project();
        project.setProjectId("testProject");
        project.setProjectName("Test Project");

        // Mock the projectService.findById method
        Mockito.when(projectService.findById("1")).thenReturn(project);

        // Call the getProject method
        ResponseEntity<Project> response = projectController.getProject("1");

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(project, response.getBody());

        // Verify that projectService.findById was called with the correct ID
        Mockito.verify(projectService).findById("1");
    }

    @Test
    void testGetProject_NonExistingProject() {
        // Mock the projectService.findById method to return null
        Mockito.when(projectService.findById("1")).thenReturn(null);

        // Call the getProject method
        ResponseEntity<Project> response = projectController.getProject("1");

        // Verify the response
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());

        // Verify that projectService.findById was called with the correct ID
        Mockito.verify(projectService).findById("1");
    }

    @Test
    void testGetAllProjects() {
        // Create a list of sample projects
        List<Project> projects = new ArrayList<>();
        Project project1 = new Project();
        project1.setProjectId("1");
        project1.setProjectName("Project 1");
        Project project2 = new Project();
        project2.setProjectId("2");
        project2.setProjectName("Project 2");
        projects.add(project1);
        projects.add(project2);

        // Mock the projectService.getAllProjects method
        Mockito.when(projectService.getAllProjects()).thenReturn(projects);

        // Call the getAllProjects method
        ResponseEntity<List<Project>> response = projectController.getAllProjects();

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(projects, response.getBody());

        // Verify that projectService.getAllProjects was called
        Mockito.verify(projectService).getAllProjects();
    }

    @Test
    void testDeleteProject_ExistingProject() {
        // Mock the projectService.deleteProject method to return true
        Mockito.when(projectService.deleteProject("1")).thenReturn(true);

        // Call the deleteProject method
        ResponseEntity<Boolean> response = projectController.deleteProject("1");

        // Verify the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody());

        // Verify that projectService.deleteProject was called with the correct ID
        Mockito.verify(projectService).deleteProject("1");
    }

    @Test
    void testDeleteProject_NonExistingProject() {
        // Mock the projectService.deleteProject method to return false
        Mockito.when(projectService.deleteProject("1")).thenReturn(false);

        // Call the deleteProject method
        ResponseEntity<Boolean> response = projectController.deleteProject("1");

        // Verify the response
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());

        // Verify that projectService.deleteProject was called with the correct ID
        Mockito.verify(projectService).deleteProject("1");
    }
}

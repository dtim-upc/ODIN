package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.DataResource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.JsonDataset;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.odin.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


/**
 * The service class for managing datasources in a project.
 */
@Service
public class SourceService {
    /**
     * The dependency on the ProjectService class.
     */
    private final ProjectService projectService;
    /**
     * The AppConfig dependency for accessing application configuration.
     */
    private final AppConfig appConfig;
    /**
     * The ORMStoreInterface dependency for storing datasets.
     */
    private final ORMStoreInterface ormDataResource;

    /**
     * Constructs a new instance of SourceService.
     *
     * @param appConfig      The AppConfig dependency for accessing application configuration.
     * @param projectService The ProjectService dependency.
     */
    public SourceService(@Autowired AppConfig appConfig,
                         @Autowired ProjectService projectService) {
        this.appConfig = appConfig;
        this.projectService = projectService;
        try {
            this.ormDataResource = ORMStoreFactory.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores a multipart file in the specified disk path and returns the absolute path of the file.
     *
     * @param multipartFile The multipart file to store.
     * @return The absolute path of the stored file.
     */
    public String reconstructFile(MultipartFile multipartFile) {
        try {
            if (multipartFile.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder(16);
            SecureRandom random = new SecureRandom();
            for (int i = 0; i < 16; i++) {
                int randomIndex = random.nextInt(characters.length());
                sb.append(characters.charAt(randomIndex));
            }

            String filename = sb + "_" + multipartFile.getOriginalFilename();

            // Get the disk path from the app configuration
            Path diskPath = Path.of(appConfig.getDiskPath());

            // Resolve the destination file path using the disk path and the generated filename
            Path destinationFile = diskPath.resolve(Paths.get(filename));

            // Perform a security check to ensure that the destination file is within the disk path
            if (!destinationFile.getParent().equals(diskPath)) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            // Copy the input stream of the multipart file to the destination file
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    /**
     * Extracts data from a file and returns a Dataset object with the extracted data.
     *
     * @param filePath           The path of the file to extract data from.
     * @param datasetName        The name of the dataset.
     * @param datasetDescription The description of the dataset.
     * @return A Dataset object with the extracted data.
     */
    public Dataset extractData(String filePath, String datasetName, String datasetDescription) {
        // Extract the extension of the file from the file path
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1);

        Dataset dataset;

        // Create a new dataset object with the extracted data
        switch (extension.toLowerCase()) {
            case "csv":
                dataset = new CsvDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "json":
                dataset = new JsonDataset(null, datasetName, datasetDescription, filePath);
                break;
            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }

        return dataset;
    }

    /**
     * Transforms a Dataset object into a Graph object representing the data in RDF format.
     *
     * @param dataset The Dataset object to transform.
     * @return A GraphModelPair object containing the transformed Graph and the corresponding Model.
     */
    public Graph transformToGraph(DataResource dataset) {
        try {
            bsModuleInterface bsInterface = new bsModuleImpl();
            return bsInterface.convertDatasetToGraph(dataset);
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException("Dataset type not supported. Something went wrong during bootstrap process generating the schema.");
        }
    }


    /**
     * Generates a visual representation of a Graph using NextiaGraphy library.
     *
     * @param graph The GraphModelPair object containing the Graph.
     * @return A String representing the visual schema of the Graph.
     */
    public String generateVisualSchema(Graph graph) {
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        return visualLibInterface.generateVisualGraph(graph);
    }

    /**
     * Saves a Graph object to the database using a GraphStoreInterface.
     *
     * @param graph The Graph object to save.
     * @return A boolean indicating whether the saving operation was successful.
     */
    public boolean saveGraphToDatabase(Graph graph) {
        GraphStoreInterface graphStore;
        try {
            graphStore = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assert graphStore != null;
        graphStore.saveGraph(graph);
        return true;
    }

    /**
     * Deletes a dataset from a project using the ProjectService class.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     */
    public void deleteDatasetFromProject(String projectId, String datasetId) {
        projectService.deleteDatasetFromProject(projectId, datasetId);
    }

    /**
     * Saves a Dataset object using the ORMStoreInterface.
     *
     * @param dataset The Dataset object to save.
     * @return The saved Dataset object.
     */
    public Dataset saveDataset(Dataset dataset) {
        return ormDataResource.save(dataset);
    }

    /**
     * Retrieves all datasets from the ORMStoreInterface.
     *
     * @return A list of Dataset objects.
     */
    public List<Dataset> getDatasets() {
        return ormDataResource.getAll(Dataset.class);
    }

    /**
     * Deletes a datasource from the ORMStoreInterface. NOT USED. When deleteDatasetFromProject(...) cascade all does this implicit.
     *
     * @param id The ID of the datasource to delete.
     * @return A boolean indicating whether the deletion was successful.
     */
    public boolean deleteDatasource(String id) {
        return ormDataResource.deleteOne(Dataset.class, id);
    }

    /**
     * Checks if a project contains a specific dataset using the ProjectService class.
     *
     * @param projectId The ID of the project to check.
     * @param id        The ID of the dataset to check.
     * @return A boolean indicating whether the project contains the dataset.
     */
    public boolean projectContains(String projectId, String id) {
        return projectService.projectContains(projectId, id);
    }

    /**
     * Retrieves all datasets of a project using the ProjectService class.
     *
     * @param id The ID of the project to retrieve datasets from.
     * @return A list of Dataset objects associated with the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
        // Returning the list of datasets associated with the project
        return projectService.getDatasetsOfProject(id);
    }

    public Dataset setLocalGraphToDataset(Dataset savedDataset, Graph graph) {
        LocalGraphJenaImpl localGraph = CoreGraphFactory.createLocalGraph();
        localGraph.setGraphName(graph.getGraphName());
        localGraph.setGraphicalSchema(graph.getGraphicalSchema());
        savedDataset.setLocalGraph(localGraph);
        return (Dataset) saveDataset(savedDataset);
    }

    public DataRepository findRepositoryById(String repositoryId) {
        return ormDataResource.findById(DataRepository.class, repositoryId);
    }

    public DataRepository createRepository(String repositoryName) {
        DataResource dataRepository = new DataRepository();
        ((DataRepository) dataRepository).setRepositoryName(repositoryName);
        return (DataRepository) ormDataResource.save(dataRepository);
    }

    public DataRepository addDatasetToRepository(String datasetId, String repositoryId) {
        // Step 1: Find the repository and dataset using their IDs
        DataRepository dataRepository = ormDataResource.findById(DataRepository.class, repositoryId);
        Dataset dataset = ormDataResource.findById(Dataset.class, datasetId);

        // Step 2: If the repository and dataset exist, add the dataset to the repository's list of datasets
        if (dataRepository != null && dataset != null) {
            List<Dataset> repoDatasets = dataRepository.getDatasets();
            repoDatasets.add(dataset);
            dataRepository.setDatasets(repoDatasets);
        }

        // Step 3: Save the changes to the repository
        dataRepository = ormDataResource.save(dataRepository);

        // Step 4: Return the updated DataRepository object
        return (DataRepository) dataRepository;
    }

    public void addRepositoryToProject(String projectId, String repositoryId) {
        DataRepository dataRepository = ormDataResource.findById(DataRepository.class, repositoryId);

        projectService.addRepositoryToProject(projectId, repositoryId);
    }

    public List<DataRepository> getRepositoriesOfProject(String id) {
        return projectService.getRepositoriesOfProject(id);
    }

    public boolean editDataset(Dataset dataset) {
        Dataset originalDataset = ormDataResource.findById(Dataset.class, dataset.getId());

        // Check if any attribute has changed
        if (!dataset.getDatasetName().equals(originalDataset.getDatasetName())
                || !dataset.getDatasetDescription().equals(originalDataset.getDatasetDescription())
        ) {
            // At least one attribute has changed
            originalDataset.setDatasetName(dataset.getDatasetName());
            originalDataset.setDatasetDescription(dataset.getDatasetDescription());

            // Perform the database update operation to save the changes
            saveDataset(originalDataset);

            return true;
        }

        // No changes detected, return false
        return true;
    }

    /**
     * Deletes a dataset from the list of datasets in a repository given a datasetId.
     *
     * @param projectId The ID of the project to find the repository.
     * @param datasetId The ID of the dataset to delete.
     */
    public void deleteDatasetFromRepo(String projectId, String datasetId) {
        // Step 1: Get the project by projectId
        Project project = projectService.getProjectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found with projectId: " + projectId);
        }

        // Step 2: Find the repository that contains the dataset with datasetId
        List<DataRepository> repositories = project.getRepositories();
        DataRepository targetRepository = null;

        for (DataRepository repository : repositories) {
            for (Dataset dataset : repository.getDatasets()) {
                if (dataset.getId().equals(datasetId)) {
                    targetRepository = repository;
                    break;
                }
            }
            if (targetRepository != null) {
                break;
            }
        }

        // Step 3: If the repository is found, remove the dataset and save the changes
        if (targetRepository != null) {
            List<Dataset> datasets = targetRepository.getDatasets();
            datasets.removeIf(dataset -> dataset.getId().equals(datasetId));
            targetRepository.setDatasets(datasets);
            ormDataResource.save(targetRepository);
        }
    }

    public boolean projectHasIntegratedGraph(String projectId) {
        // Retrieve the project with the given ID
        Project project = projectService.getProjectById(projectId);

        // If the project is not found, throw an exception
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        // Check if the project has an integrated graph assigned
        return project.getIntegratedGraph() != null;
    }

    public void setProjectSchemasBase(String projectId, String datasetId) {
        // Retrieve the project with the given ID
        Project project = projectService.getProjectById(projectId);

        // If the project is not found, throw an exception
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        // Find the dataset with the given datasetId
        Dataset dataset = ormDataResource.findById(Dataset.class, datasetId);

        // If the dataset is not found in the project, throw an exception
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found in the project");
        }

        // Check if the project already has an integrated graph assigned
        if (project.getIntegratedGraph() != null) {
            // If the project already has an integrated graph, throw an exception or handle it accordingly
            throw new IllegalArgumentException("Project already has an integrated graph");
        }

        // Assign the schema of the dataset to the project's integrated graph
        try {
            Graph integratedGraph = CoreGraphFactory.createIntegratedGraph();
            GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);

            Graph datasetGraph = graphStore.getGraph(dataset.getLocalGraph().getGraphName());

            integratedGraph.setGraphName(null);
            integratedGraph.setGraph(datasetGraph.getGraph());
            integratedGraph.setGraphicalSchema(datasetGraph.getGraphicalSchema());

            project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            projectService.saveProject(project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}



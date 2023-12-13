package edu.upc.essi.dtim.odin.frontend;

import edu.upc.essi.dtim.odin.repositories.POJOs.DataRepositoryTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@RestController
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);
    // Complete path of the file that contains the templates of the repositories.
    private static final String repositoryFormsPath = "api\\src\\main\\resources\\frontend-schemas\\RepositoryForms\\";

    /**
     * Retrieves the basic template of the repositories
     *
     * @return A String containing the template.
     */
    @GetMapping("/formSchema")
    public String getFormSchema() {
        logger.info("Formschema asked");
        try {
            String filePath = repositoryFormsPath + "Template_Repository.json";
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String formSchema = new String(bytes);
                logger.info("Formschema retrieved");
                return formSchema;
            } else {
                logger.error("Formschema not found");
                return "Field was not found";
            }
        } catch (Exception e) {
            logger.error("Formschema error: " + e);
            // TODO: manage errors appropiately
            return null;
        }
    }

    /**
     * Retrieves the template of a specific type of repository
     *
     * @param fileName Type of template to be retrieved.
     * @return A String containing the template.
     */
    @GetMapping("/formSchema/{fileName}")
    public String getConcreteFormSchema(@PathVariable("fileName") String fileName) {
        logger.info("Formschema asked: " + fileName);
        try {
            String filePath = repositoryFormsPath + fileName;
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String formSchema = new String(bytes);
                logger.info("Formschema retrieved");
                return formSchema;
            } else {
                logger.error("Formschema not found");
                return "File not found";
            }
        } catch (Exception e) {
            logger.error("Formschema error: " + e);
            // TODO: manage errors appropiately
            return null;
        }
    }

    /**
     * Retrieves all templates of all repositories
     *
     * @return A List<DataRepositoryTypeInfo> containing the different templates.
     */
    @GetMapping("/api/data-repository-types")
    public List<DataRepositoryTypeInfo> getAllDataRepositoryTypes() {
        List<DataRepositoryTypeInfo> dataRepositoryClasses = new ArrayList<>();

        File directory = new File(repositoryFormsPath);

        if (directory.isDirectory()) {
            File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));

            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    String fileName = jsonFile.getName();
                    String displayName = fileName.replace("_", " ").replace(".json", "");

                    DataRepositoryTypeInfo dataRepositoryTypeInfo = new DataRepositoryTypeInfo(displayName, fileName);
                    dataRepositoryClasses.add(dataRepositoryTypeInfo);
                }
            }
        }

        return dataRepositoryClasses;
    }

}

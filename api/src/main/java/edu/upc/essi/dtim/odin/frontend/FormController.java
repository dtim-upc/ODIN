package edu.upc.essi.dtim.odin.frontend;

import edu.upc.essi.dtim.odin.repositories.POJOs.DataRepositoryTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@RestController
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);
    private final FormService formService;
    // Complete path of the file that contains the templates of the repositories.
    private static final String repositoryFormsPath = "api\\src\\main\\resources\\frontend-schemas\\RepositoryForms\\";

    public FormController(FormService formService) {
        this.formService = formService;
    }

    /**
     * Retrieves the basic template of the repositories
     *
     * @return A String containing the template.
     */
    @GetMapping("/formSchema")
    public String getFormSchema() {
        logger.info("Formschema asked");
        try {
            return formService.getRepositorySchema(repositoryFormsPath + "Template_Repository.json");
        } catch (Exception e) {
            logger.error("Formschema error: " + e);
            return null;
        }
    }

    /**
     * Retrieves the template of a specific type of repository
     *
     * @param repositoryType Type of template to be retrieved.
     * @return A String containing the template.
     */
    @GetMapping("/formSchema/{repositoryType}")
    public String getSpecificFormSchema(@PathVariable("repositoryType") String repositoryType) {
        logger.info("Formschema asked: " + repositoryType);
        try {
            return formService.getRepositorySchema(repositoryFormsPath + repositoryType);
        } catch (Exception e) {
            logger.error("Formschema error: " + e);
            return null;
        }
    }

    /**
     * Returns a list will all the types of repositories available in the system
     *
     * @return A List of DataRepositoryTypeInfo, objects with two variables: repository name and repository file name.
     */
    @GetMapping("/api/data-repository-types")
    public List<DataRepositoryTypeInfo> getDataRepositoryTypes() {
        return formService.getDataRepositoryTypes(repositoryFormsPath);
    }

}

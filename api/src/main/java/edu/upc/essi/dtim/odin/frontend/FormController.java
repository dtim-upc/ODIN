package edu.upc.essi.dtim.odin.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    @GetMapping("/formSchema")
    public String getFormSchema() {
        logger.info("FORMSCHEMA ASKED");
        try {
            // Especifica la ruta completa al archivo en el sistema de archivos local.
            String filePath = "../api/src/main/resources/frontend-schemas/RepositoryForms/DatasetForm.json";
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String formSchema = new String(bytes);
                logger.info("FORMSCHEMA RETRIEVED");
                return formSchema;
            } else {
                logger.error("FORMSCHEMA not found");
                // El archivo no existe en la ubicación especificada.
                return "El archivo no se encontró.";
            }
        } catch (Exception e) {
            logger.error("FORMSCHEMA ERROR: " + e);
            // Manejar errores apropiadamente
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/formSchema/{fileName}")
    public String getConcreteFormSchema(@PathVariable("fileName") String fileName) {
        logger.info("FORMSCHEMA ASKED: "+fileName);
        try {
            // Especifica la ruta completa al archivo en el sistema de archivos local.
            String filePath = "../api/src/main/resources/frontend-schemas/RepositoryForms/"+fileName;
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String formSchema = new String(bytes);
                logger.info("FORMSCHEMA RETRIEVED");
                return formSchema;
            } else {
                logger.error("FORMSCHEMA not found");
                // El archivo no existe en la ubicación especificada.
                return "El archivo no se encontró.";
            }
        } catch (Exception e) {
            logger.error("FORMSCHEMA ERROR: " + e);
            // Manejar errores apropiadamente
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/data-repository-types")
    public List<DataRepositoryTypeInfo> getAllDataRepositoryTypes() {
        return getAllDataRepositoryTypesService("../api/src/main/resources/frontend-schemas/RepositoryForms");
    }

    public List<DataRepositoryTypeInfo> getAllDataRepositoryTypesService(String directoryPath) {
        List<DataRepositoryTypeInfo> dataRepositoryClasses = new ArrayList<>();

        File directory = new File(directoryPath);

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

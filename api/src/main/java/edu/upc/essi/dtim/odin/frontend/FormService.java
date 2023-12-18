package edu.upc.essi.dtim.odin.frontend;

import edu.upc.essi.dtim.odin.repositories.POJOs.DataRepositoryTypeInfo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FormService {
    public String getRepositorySchema(String filePath) {
        Resource resource = new FileSystemResource(filePath);

        if (resource.exists()) {
            byte[] bytes;
            try {
                bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String formSchema = new String(bytes);
            System.out.println("Formschema retrieved");
            return formSchema;
        } else {
            System.out.println("Formschema not found");
            return "File not found";
        }
    }

    public List<DataRepositoryTypeInfo> getDataRepositoryTypes(String repositoryFormsPath) {
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

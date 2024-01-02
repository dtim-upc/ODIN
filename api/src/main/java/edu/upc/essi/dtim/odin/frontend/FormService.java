package edu.upc.essi.dtim.odin.frontend;

import edu.upc.essi.dtim.odin.exception.CustomIOException;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
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
                throw new CustomIOException(e.getMessage());
            }
            return new String(bytes);
        } else {
            throw new ElementNotFoundException("Resource could not be found in " + filePath);
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
        else {
            throw new ElementNotFoundException("Directory does not exist: " + repositoryFormsPath);
        }
        return dataRepositoryClasses;
    }
}

package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JsonDataset extends Dataset{
    public JsonDataset(){
        super();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    /**
     * Constructor for the JsonDataset class.
     *
     * @param id          The ID of the dataset.
     * @param name        The name of the dataset.
     * @param description A description of the dataset.
     * @param path        The path to the dataset file.
     */
    public JsonDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid file format. Only JSON files are supported.");
        }
        else {
            try {
                List<Attribute> attributes = new LinkedList<>();

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonArray = objectMapper.readTree(new File(path));

                if (jsonArray.isArray()) {
                    // Assuming all objects in the array have the same keys
                    JsonNode firstObject = jsonArray.get(0);
                    Iterator<String> fieldNames = firstObject.fieldNames();

                    while (fieldNames.hasNext()) {
                        String key = fieldNames.next();
                        attributes.add(new Attribute(key, ""));
                    }
                } else {
                    System.out.println("The JSON data is not an array.");
                }
                super.setAttributes(attributes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.path = path;
        }
    }
}

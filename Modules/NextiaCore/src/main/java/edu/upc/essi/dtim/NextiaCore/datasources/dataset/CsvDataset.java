package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

import com.opencsv.CSVReader;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CsvDataset extends Dataset{

    public CsvDataset(){
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
     * Constructor for the CsvDataset class.
     *
     * @param id          The ID of the dataset.
     * @param name        The name of the dataset.
     * @param description A description of the dataset.
     * @param path        The path to the dataset file.
     */
    public CsvDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file format. Only CSV files are supported.");
        }
        else {
            List<Attribute> attributes = new LinkedList<>();
            try (CSVReader reader = new CSVReader(new FileReader(path))) {
                String[] columnNames = reader.readNext(); // Read the first row as column names
                if (columnNames != null) {
                    for (String columnName : columnNames) {
                        attributes.add(new Attribute(columnName, ""));
                    }
                } else {
                    throw new IllegalArgumentException("No column names found in the CSV file.");
                }
                super.setAttributes(attributes);
            } catch (IOException e) {
                //e.printStackTrace();
            }
            this.path = path;
        }
    }
}

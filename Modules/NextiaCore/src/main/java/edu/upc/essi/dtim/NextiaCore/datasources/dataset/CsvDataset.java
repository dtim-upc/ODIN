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
            this.path = path;
        }
    }
}

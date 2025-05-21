package edu.upc.essi.dtim.NextiaCore.mappings;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;

import java.io.InputStream;

public class MappingsConfig extends Mappings {
    private InputStream configFile;

    public MappingsConfig(String mappingType, InputStream configInputStream, Graph graphI) {
        super(mappingType, graphI);
        this.configFile = configInputStream;
    }

    public InputStream getConfigFile() {
        return configFile;
    }

    public void setConfigPath(InputStream configPath) {
        this.configFile = configPath;
    }
}

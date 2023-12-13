package edu.upc.essi.dtim.odin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to manage application properties.
 */
@Configuration
public class AppConfig {

    /**
     * Constructor for the AppConfig class.
     * The constructor implementation is empty, as no additional logic is required at the moment.
     */
    public AppConfig() {
        // Constructor implementation is empty.
        // No additional logic is required at the moment.
    }

    /**
     * Database type property retrieved from application.properties.
     */
    @Value("${dataStorage.DataBaseType}")
    public String DBTypeProperty;

    /**
     * Disk path property retrieved from application.properties.
     */
    @Value("${dataStorage.DataLayerPath}")
    private String DataLayerPath;

    /**
     * Jena path property retrieved from application.properties.
     */
    @Value("${dataStorage.JenaPath}")
    private String JenaPath;

    @Value("${dataStorage.DataLayerTechnology}")
    private String DataLayerTechnology;

    public String getDBTypeProperty() {
        return DBTypeProperty;
    }

    public String getDataLayerPath() {
        return DataLayerPath;
    }

    public String getJenaPath() {
        return JenaPath;
    }

    public String getDataLayerTechnology() {
        return DataLayerTechnology;
    }
}

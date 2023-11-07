package edu.upc.essi.dtim.odin.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to manage application properties.
 */
@Getter
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
    @Value("${dataStorage.diskPath}")
    public String diskPath;

    /**
     * Jena path property retrieved from application.properties.
     */
    @Value("${dataStorage.JenaPath}")
    private String JenaPath;

    @Value("${dataStorage.DataLayerPath}")
    private String DataLayerPath;
}

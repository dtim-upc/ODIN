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

    /**
     * Tool used to implement the data layer
     */
    @Value("${dataStorage.DataLayerTechnology}")
    private String DataLayerTechnology;

    /**
     * Tool used to persist data for integration with Cyclops
     */
    @Value("${lts.CyclopsLTS}")
    private String CyclopsLTS;

    /**
     * MinIO endpoint property retrieved from application.properties.
     */
    @Value("${lts.endpoint}")
    private String MinIOEndpoint;

    /**
     * MinIO access key property retrieved from application.properties.
     */
    @Value("${lts.accessKey}")
    private String MinIOAccessKey;

    /**
     * MinIO secret key property retrieved from application.properties.
     */
    @Value("${lts.secretKey}")
    private String MinIOSecretKey;

    /**
     * MinIO bucket property retrieved from application.properties.
     */
    @Value("${lts.bucket}")
    private String MinIOBucket;



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

    public String getCyclopsLTS() {
        return CyclopsLTS;
    }

    public String getMinIOEndpoint() { return MinIOEndpoint;}

    public String getMinIOAccessKey() {return MinIOAccessKey;}

    public String getMinIOSecretKey() {return MinIOSecretKey;}

    public String getMinIOBucket() {return MinIOBucket;}
}

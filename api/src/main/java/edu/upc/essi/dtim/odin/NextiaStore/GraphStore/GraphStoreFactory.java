package edu.upc.essi.dtim.odin.NextiaStore.GraphStore;

import edu.upc.essi.dtim.odin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory class responsible for creating instances of {@link GraphStoreInterface}
 * implementations based on the specified database type ({@code dbType}) from an {@link AppConfig} object.
 * It ensures that only one instance of the selected implementation is created and returned
 * for the lifetime of the application.
 */
@Component
public class GraphStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(GraphStoreFactory.class);
    private static GraphStoreInterface instance = null;

    /**
     * Private constructor prevents instantiation from outside the class.
     */
    private GraphStoreFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    /**
     * Get an instance of {@link GraphStoreInterface} based on the specified database type
     * and configuration.
     *
     * @param appConfig The application configuration containing database type information.
     * @return An instance of {@link GraphStoreInterface} corresponding to the specified database type.
     * @throws RuntimeException If there is an error creating the instance or if the database type is not recognized.
     */
    public static GraphStoreInterface getInstance(AppConfig appConfig){
        if (appConfig == null) {
            throw new IllegalArgumentException("appConfig cannot be null");
        }

        String dbType = appConfig.getDBTypeProperty();
        if (instance == null) {
            logger.info("Creating a new instance of GraphStoreFactory with DB type: {}", dbType);

            switch (dbType) {
                case "JENA":
                    instance = new GraphStoreJenaImpl(appConfig);
                    break;
                case "NEO4J":
                    // Add other implementations here.
                    break;
                default:
                    throw new RuntimeException("Error with DB type: " + dbType);
            }
        }
        return instance;
    }
}

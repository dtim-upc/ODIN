package edu.upc.essi.dtim.odin.NextiaStore.GraphStore;

import edu.upc.essi.dtim.odin.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GraphStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(GraphStoreFactory.class);
    private static GraphStoreInterface instance = null;

    private GraphStoreFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static GraphStoreInterface getInstance(AppConfig appConfig) throws Exception {
        if (appConfig == null) {
            throw new IllegalArgumentException("appConfig cannot be null");
        }

        String dbType = appConfig.getDBTypeProperty();
            if (instance == null) {
                logger.info("Creating new instance of GraphStoreFactory with DB type: {}", dbType);

                switch (dbType) {
                    case "JENA":
                        instance = new GraphStoreJenaImpl(appConfig);
                        break;
                    case "NEO4J":
                    //OTHER IMPLEMENTATIONS
                    default:
                        throw new Exception("Error with DB type");
                }
            }
            return instance;
    }
}

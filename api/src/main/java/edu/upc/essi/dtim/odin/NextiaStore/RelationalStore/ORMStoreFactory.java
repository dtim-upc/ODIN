package edu.upc.essi.dtim.odin.NextiaStore.RelationalStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory class responsible for creating instances of {@link ORMStoreInterface}.
 * It ensures that only one instance of the ORM implementation is created and returned
 * for the lifetime of the application.
 */
@Component
public class ORMStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(ORMStoreFactory.class);
    private static ORMStoreInterface ormStoreInterfaceInstance = null;

    /**
     * Private constructor prevents instantiation from outside the class.
     */
    private ORMStoreFactory() {
        // Private constructor to prevent instantiation from outside the class
    }

    /**
     * Get an instance of {@link ORMStoreInterface}.
     * If an instance does not exist, a new instance of the default ORM implementation {@link ORMStoreJpaImpl} is created.
     *
     * @return An instance of {@link ORMStoreInterface}.
     */
    public static ORMStoreInterface getInstance() {
        if (ormStoreInterfaceInstance == null) {
            logger.info("Creating a new instance of JpaOrmImplementation");
            ormStoreInterfaceInstance = new ORMStoreJpaImpl();
        }
        return ormStoreInterfaceInstance;
    }
}

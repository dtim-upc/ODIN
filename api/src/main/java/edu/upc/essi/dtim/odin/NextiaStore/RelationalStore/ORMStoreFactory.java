package edu.upc.essi.dtim.odin.NextiaStore.RelationalStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ORMStoreFactory {
    private static final Logger logger = LoggerFactory.getLogger(ORMStoreFactory.class);

    private static ORMStoreInterface ormStoreInterfaceInstance = null;

    private ORMStoreFactory() {
        // Private constructor to prevent instantiation from outside the class
    }

    public static ORMStoreInterface getInstance() {
        if (ormStoreInterfaceInstance == null) {
            logger.info("Creating new instance of JpaOrmImplementation");
            ormStoreInterfaceInstance = new OrmStoreJpaImpl();
        }
        return ormStoreInterfaceInstance;
    }
}

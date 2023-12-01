package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaDataLayer.implementations.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

public final class DataLayerSingleton {
    private static DataLayer instance;

    public static DataLayer getInstance(@Autowired AppConfig appConfig) {
        if (instance == null) {
            try {
                instance = DataLayerFactory.getInstance(appConfig.getDataLayerTechnology(), appConfig.getDataLayerPath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
}

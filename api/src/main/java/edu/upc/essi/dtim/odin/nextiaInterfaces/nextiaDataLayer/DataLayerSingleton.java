package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Data layer can be implemented by several data management systems. As such, we have ti guarantee that there is
 * only one implementation of the data layer at a time, and that it does not change, hence the singleton.
 */
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

package edu.upc.essi.dtim.odin.nextiaInterfaces.cyclopsLTS;

import edu.upc.essi.dtim.CyclopsLTS.config.LTSConfig;
import edu.upc.essi.dtim.CyclopsLTS.config.LTSConfigMinIO;
import edu.upc.essi.dtim.CyclopsLTS.lts.LTS;
import edu.upc.essi.dtim.CyclopsLTS.utils.LTSFactory;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class LTSSingleton {
    private static LTS instance;

    public static LTS getInstance(@Autowired AppConfig appConfig) {
        if (instance == null) {
            try {
                String tech = appConfig.getCyclopsLTS();
                LTSConfig ltsConfig;

                switch (tech) {
                    case "MinIO":
                        ltsConfig = new LTSConfigMinIO(
                                appConfig.getDataLayerTechnology(),
                                appConfig.getDataLayerPath(),
                                appConfig.getMinIOEndpoint(),
                                appConfig.getMinIOAccessKey(),
                                appConfig.getMinIOSecretKey(),
                                appConfig.getMinIOBucket()
                        );
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported LTS technology: " + tech);
                }
                instance = LTSFactory.getInstance(ltsConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
}

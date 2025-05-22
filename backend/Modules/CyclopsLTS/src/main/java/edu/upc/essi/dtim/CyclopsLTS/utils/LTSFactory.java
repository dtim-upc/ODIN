package edu.upc.essi.dtim.CyclopsLTS.utils;

import edu.upc.essi.dtim.CyclopsLTS.lts.LTS;
import edu.upc.essi.dtim.CyclopsLTS.lts.LTSMinIO;
import edu.upc.essi.dtim.CyclopsLTS.config.LTSConfig;

public class LTSFactory {
    private static LTS instance = null;

    private LTSFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static LTS getInstance(LTSConfig ltsconf) throws Exception {
        switch (ltsconf.getTechnology()) {
            case "MinIO":
                instance = new LTSMinIO(ltsconf.getPath(), ltsconf.getEndpoint(),  ltsconf.getAccessKey(),  ltsconf.getSecretKey(), ltsconf.getBucket());
                break;
            default:
                instance = new LTSMinIO(ltsconf.getPath(), ltsconf.getEndpoint(),  ltsconf.getAccessKey(),  ltsconf.getSecretKey(), ltsconf.getBucket());
        }
        return instance;
    }
}

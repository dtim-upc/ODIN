package edu.upc.essi.dtim.CyclopsLTS.lts;
import edu.upc.essi.dtim.NextiaCore.datasets.*;

import java.sql.SQLException;

public abstract class LTS {
    String dataStorePath;

    public LTS(String dataStorePath) {
        this.dataStorePath = dataStorePath;
    }

    public abstract void uploadToLTS(Dataset d, String tableName) throws SQLException;

    public abstract void removeFromLTS(String tableName);

    public abstract void close();
}

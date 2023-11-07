package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;

public final class DataLoadingSingleton {
    private static DataLoading instance;

    /**
     * @param value The path of the DataLoading
     * @return The singleton instance of DataLoading
     */
    public static DataLoading getInstance(String value) {
        if (null == instance) {
            instance = new DataLoading(value);
        }
        return instance;
    }
}

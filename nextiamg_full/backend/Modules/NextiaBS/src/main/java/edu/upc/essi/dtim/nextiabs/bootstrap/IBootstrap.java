package edu.upc.essi.dtim.nextiabs.bootstrap;

import edu.upc.essi.dtim.nextiabs.implementations.IDataSource;

public interface IBootstrap<T> extends IDataSource {

    // Each class's constructor will take the appropriate arguments and then run bootstrapSchema
    T bootstrapSchema();
    T bootstrapSchema(Boolean generateMetadata);

    void generateMetadata();

}

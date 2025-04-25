package edu.upc.essi.dtim.nextiamg.mappingsGeneration;

import edu.upc.essi.dtim.nextiamg.mappingsImplementation.IMappingType;

public interface IMapgen<T> extends IMappingType {

    // Each class's constructor will take the appropriate arguments and then run bootstrapSchema
    T generateMappings();

}

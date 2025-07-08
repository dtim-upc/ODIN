package edu.upc.essi.dtim.nextiacd;

import edu.upc.essi.dtim.NextiaCore.constraints.DenialConstraint;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;

import java.util.List;

public interface IConstraintDiscovery {
    List<DenialConstraint> getDCs(Dataset d1) throws Exception;
    String getIdentifier(Dataset dataset) throws Exception;
}

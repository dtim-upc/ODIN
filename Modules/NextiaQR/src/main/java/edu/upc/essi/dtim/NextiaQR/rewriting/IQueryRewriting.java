package edu.upc.essi.dtim.NextiaQR.rewriting;


import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;

import java.util.List;

public interface IQueryRewriting {

    public void generateQueryingStructures(IntegratedGraph IG, List<Dataset> integratedDatasets);

    //double calculateJoinQualityDiscreteFromCSV(String CSVPath1, String CSVPath2, String att1, String att2);

    //double calculateJoinQualityContinuousFromCSV(String CSVPath1, String CSVPath2, String att1, String att2);

}

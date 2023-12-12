package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.JsonDataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        CSVBootstrap_with_DataFrame_MM_without_Jena cbs = new CSVBootstrap_with_DataFrame_MM_without_Jena();
        CsvDataset d1 = new CsvDataset("id", "titanic", "description", "C:\\Work\\Files\\titanic.csv");

//        JSONBootstrap_with_DataFrame_MM_without_Jena jbs = new JSONBootstrap_with_DataFrame_MM_without_Jena();
//        JsonDataset d2 = new JsonDataset("cats", "cats", "description", "C:\\Work\\Files\\cats_simplified.json");

        BootstrapResult bsr = cbs.bootstrap(d1);
        System.out.println(bsr.getWrapper());
    }
}

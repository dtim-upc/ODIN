package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.JsonDataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        CSVBootstrap_with_DataFrame_MM_without_Jena cbs = new CSVBootstrap_with_DataFrame_MM_without_Jena();
        CsvDataset d1 = new CsvDataset("id", "zillow", "description", "C:\\Work\\Files\\test_datasets\\zillow.csv");

        JSONBootstrap_with_DataFrame_MM_without_Jena jbs = new JSONBootstrap_with_DataFrame_MM_without_Jena();
        JsonDataset d2 = new JsonDataset("cats", "cats", "description", "C:\\Work\\Files\\cats_simplified.json");

        BootstrapResult bsr = jbs.bootstrap(d2);
        System.out.println(bsr.getWrapper());

//        BootstrapResult bsr = jbs.bootstrap(d);
//        System.out.println(bsr.getWrapper());
    }
}

package edu.upc.essi.dtim.nextiamg;

import edu.upc.essi.dtim.NextiaCore.datasets.CSVDataset;
import edu.upc.essi.dtim.NextiaCore.datasets.JSONDataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;

public class Main {

    public static void main(String[] args) {
//        CSVDataset d1 = new CSVDataset("id", "titanic", "description", "C:\\Work\\Files\\test_datasets\\hooke.csv");
//        CSVBootstrap cbs = new CSVBootstrap(d1.getId(), d1.getDatasetName(), d1.getPath());

//        JSONDataset d2 = new JSONDataset("cats", "cats", "description", "C:\\Work\\Files\\test.json");
//        JSONBootstrap jbs = new JSONBootstrap("test", "test", "C:\\Work\\Files\\tests_json\\sub_object_with_arrays_literals.json");
//
//        BootstrapResult bsr = jbs.bootstrapDataset(d2);
//        System.out.println(bsr.getWrapper());
    }
//
//    public void mainCSVBootstrap() {
//        String pathcsv = "src/main/resources/artworks.csv";
//        CSVBootstrap csv = new CSVBootstrap("12","artworks", pathcsv);
//        Graph m = csv.bootstrapSchema(true);
//        PrintGraph.printGraph(m);
//    }
//
//    public void mainParquetBootstrap() {
//        String pathcsv = "src/main/resources/artwork.parquet";
//        ParquetBootstrap csv = new ParquetBootstrap("12","artworks", pathcsv);
//        Graph m =csv.bootstrapSchema(true);
//        PrintGraph.printGraph(m);
//    }
//
//    public void mainSQLBootstrap() {
//        SQLBootstrap sql =
//                new SQLBootstrap(
//                        "18",
//                        "TaulaEmpleats",
//                        "empleats",
//                        new PostgresSQLImpl(),//Database type: postgres, mysql...
//                        "localhost",
//                        "5432",
//                        "postgres",
//                        "1234", "");
//        Graph m = sql.bootstrapSchema(true);
//        PrintGraph.printGraph(m);
//    }
//
//    public void mainXMLBootstrap() {
//        String pathcsv = "src/main/resources/museums-and-galleries-1.xml";
//        XMLBootstrap csv = new XMLBootstrap("12","artworks", pathcsv);
//        Graph m =csv.bootstrapSchema(true);
//
//        PrintGraph.printGraph(m);
//    }
//
//    public void mainJSONBootstrap() {
//        String D = "stations.json";
//        JSONBootstrap j = new JSONBootstrap("stations", D,"src/main/resources/prueba_presentacion3.json");
//
////		Model M = j.bootstrapSchema("ds1", D,"/Users/javierflores/Documents/upc/projects/newODIN/datasources/survey_prueba/selected/tate_artist_picasso-pablo-1767.json");
//        Graph M = j.bootstrapSchema();
//
////        DF_MMtoRDFS translate = new DF_MMtoRDFS();
////        Graph x = translate.productionRulesDataframe_to_RDFS(M);
//
//        PrintGraph.printGraph(M);
//
////        x.setPrefixes(M.getModel().getNsPrefixMap());
////        x.write("src/main/resources/out/stations_targetPRUEBA.ttl", "Lang.TURTLE");
//
////        Graph G = new Graph();
////        java.nio.file.Path temp = Files.createTempFile("bootstrap",".ttl");
////        System.out.println("Graph written to "+temp);
////        G.write(temp.toString(), Lang.TURTLE);
////
////        System.out.println("Attributes");
////        System.out.println(j.getAttributesSWJ());
////
////        System.out.println("Source attributes");
////        System.out.println(j.getSourceAttributes());
////
////        System.out.println("Lateral views");
////        System.out.println(j.getLateralViews());
////
////        HashMap<String, JSON_Aux> attributes = j.getAttributesSWJ();
////        List<Pair<String,String>> lateralViews = j.getLateralViews();
////
////        String SELECT = attributes.entrySet().stream().map( p -> {
////            if (p.getKey().equals(p.getValue().getKey())) return p.getValue().getPath();
//////			else if (p.getKey().contains("ContainerMembershipProperty")) return p.getValue();
////            return  p.getValue().getPath() + " AS " + p.getValue().getLabel();
////        }).collect(Collectors.joining(","));
////
//////		String SELECT = attributes.stream().map(p -> {
//////			if (p.getLeft().equals(p.getRight())) return p.getLeft();
//////			else if (p.getLeft().contains("ContainerMembershipProperty")) return p.getRight();
//////			return p.getRight() + " AS " + p.getRight().replace(".","_");
//////		}).collect(Collectors.joining(","));
////        String FROM = D;
////        String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode("+p.getLeft()+") AS "+p.getRight()).collect(Collectors.joining("\n"));
////
////        String impl = "SELECT " + SELECT + " FROM " + D + " " + LATERAL;
////        System.out.println(impl);
//
//
////        j.getG_source().write("src/main/resources/out/stations_source2.ttl", Lang.TURTLE);
////        j.getG_target().write("src/main/resources/out/stations_target2.ttl", Lang.TURTLE);
//    }
}

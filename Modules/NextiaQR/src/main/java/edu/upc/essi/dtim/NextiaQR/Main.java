package edu.upc.essi.dtim.NextiaQR;

public class Main {
    public static void main(String[] args)  {
        System.out.println("Hello World");
        //CsvDataset d1 = new CsvDataset("d1", "d1", "description1", "src\\main\\resources\\sample.csv");
        //CsvDataset d2 = new CsvDataset("d2", "d2", "description2", "src\\main\\resources\\sample2.csv");

        //SQLDataset d7 = new SQLDataset("sql", "personas", "", "personas", "dtim.essi.upc.edu", "5432", "vasenjo", "jBGRfEu");
        //Attribute a1 = new Attribute("id", "string");
        //Attribute a2 = new Attribute("edad", "string");
        //Attribute a3 = new Attribute("nombre", "string");
        //List<Attribute> atts = new LinkedList<>();
        //atts.add(a1); atts.add(a2); atts.add(a3);
        //d7.setAttributes(atts);
        //RelationalJDBCRepository dr = new RelationalJDBCRepository();
        //dr.setUrl("jdbc:postgresql://dtim.essi.upc.edu:5432/odin_test");
        //dr.setVirtual(true);
        //d7.setRepository(dr);
        //d7.setUUID("personas");
        //d7.setWrapper("SELECT * FROM personas\n");

        //DataLayer dl = new DLDuckDB("C:\\Work\\Database");
        //Discovery d = new Discovery(dl);
        //List<Alignment> al = d.getAlignments(d7,d7);
        //for (Alignment a : al) {
            //    System.out.println(a.getSimilarity());
        //}

//        Connection conn = DuckDB.getConnection();
//        Profile p = new Profile(conn);
//        // if pathToStoreProfile is left blank (i.e. "") the profile will not be stored in disk
//        // if resultingProfileName is left blank (i.e. "") the profile file name will be the same as the original csv
//        p.createProfile("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\Profiles", "test2");
//        p.createProfile("D:\\Work\\TFM\\Others\\eo_xx.csv", "", "");


//        CalculateQuality cq = new CalculateQuality(conn, 4.0, 1);
//        System.out.println(cq.calculateQualityDiscrete("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\TFM\\Others\\eo_xx.csv", "ein", "ein"));
//        System.out.println(cq.calculateQualityContinuous("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\TFM\\Others\\eo_xx.csv", "ein", "ein"));

//        PredictQuality pq = new PredictQuality(conn);
//        pq.predictQuality("D:\\Projects\\Files\\eo4_profile.json", "D:\\Projects\\Files\\eo_xx_2_profile.json", "NAME", "NAME");

//        generateAllProfilesOfAllDataInAFolder("D:\\Work\\TFM\\Gittables\\Datasets", "C:\\Users\\34601\\Desktop\\gittables");
    }
}
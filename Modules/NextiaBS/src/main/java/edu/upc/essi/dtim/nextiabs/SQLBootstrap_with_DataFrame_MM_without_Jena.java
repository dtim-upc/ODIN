package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataSourceVocabulary;
import edu.upc.essi.dtim.NextiaCore.vocabulary.Formats;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.XSD;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.temp.PrintGraph;
import edu.upc.essi.dtim.nextiabs.utils.*;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import org.apache.jena.atlas.lib.Pair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * Generates an instance of a DataFrame_Metamodel representation of a postgresSQL database
 * @author juane
 */
public class SQLBootstrap_with_DataFrame_MM_without_Jena extends DataSource implements IBootstrap<Graph>, NextiaBootstrapInterface {

    private IDatabaseSystem Database;
    private SQLTableData tableData;
    private String hostname;
    private String port;
    private String username;
    private String password;
    private String tableName;

    private String databasename;


    public SQLBootstrap_with_DataFrame_MM_without_Jena(String id, String schemaName, String tableName, IDatabaseSystem DBType, String hostname, String port, String username, String password, String databasename) {
        super();
        this.id = id;
        this.name = schemaName;
        G_target = CoreGraphFactory.createGraphInstance("local");
        this.Database = DBType;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
        this.tableData = new SQLTableData(tableName);
        this.databasename = databasename;
    }

    public SQLBootstrap_with_DataFrame_MM_without_Jena() {
    }

    @Override
    public Graph bootstrapSchema() {
        return bootstrapSchema(false);
    }

    @Override
    public Graph bootstrapSchema(Boolean generateMetadata) {

        Database.connect(hostname, port,  username, password, databasename);

        //Table's raw data
        tableData = Database.getMetamodelSingleTable(tableName);

        //Una sola tabla desde SQLMetamodelTable -- a --> DataFrame_MM
        productionRules_SQL_to_DF_MM(tableName);

        //el wrapper sería un "SELECT * FROM TABLE"
        wrapper = generateWrapper();

        if(generateMetadata) {
            generateMetadata();
        }

        DF_MMtoRDFS translate = new DF_MMtoRDFS();
        G_target = translate.productionRulesDataframe_to_RDFS(G_target);

        return G_target;
    }

    private String generateWrapper() {
        String wrapper = "SELECT ";
        List<String> columns = new LinkedList<>();
        for(Pair<String, String> col: tableData.getColumns())
            columns.add(col.getLeft());
        String columnNames = String.join(", ", columns);

        wrapper += columnNames;
        wrapper += " FROM ";
        wrapper += tableName;
        System.out.println("Generated wrapper: "+wrapper);
        return wrapper;
    }

    private void productionRules_SQL_to_DF_MM(String tableName) {

//      Rule 1: Instances of sql:Table are translated to instances of DF_MM:DataFrame.
        G_target.addTriple(createIRI(tableName), RDF.type, DataFrame_MM.DataFrame);
        G_target.addTripleLiteral(createIRI(tableName), RDFS.label, tableName);

//      Rule 2: Instances of sql:Column are translated to instances of DF_MM:Data.
        for(Pair<String, String> col: tableData.getColumns()){
            G_target.addTriple(createIRI(col.getLeft()), RDF.type, DataFrame_MM.Data);
            G_target.addTripleLiteral(createIRI(col.getLeft()), RDFS.label,col.getLeft());
            G_target.addTriple(createIRI(tableName), DataFrame_MM.hasData,createIRI(col.getLeft()));
            System.out.println(col.getRight());
            if(col.getRight().equals("integer"))
                G_target.addTriple(createIRI(col.getLeft()),DataFrame_MM.hasDataType, DataFrame_MM.Number );
            else
                G_target.addTriple(createIRI(col.getLeft()),DataFrame_MM.hasDataType, DataFrame_MM.String );
        }
    }

    private String DBTypeToRDFSType(String type) {
        switch (type.toUpperCase()) {
            case "CHAR":
            case "VARCHAR":
            case "LONGVARCHAR":
            case "NCHAR":
            case "NVARCHAR":
            case "LONGNVARCHAR":
            case "INTERVAL DAY-TIME":
            case "INTERVAL YEAR-MONTH":
                return XSD.xstring;
//            case "BINARY":
//            case "VARBINARY":
//            case "LONGVARBINARY":
//                return XSD.base64Binary;
//            case "BOOLEAN":
//                return XSD.xboolean;
//            case "SMALLINT":
//                return XSD.xshort;
            case "INTEGER":
                return XSD.xint;
//            case "BIGINT":
//                return XSD.xlong;
//            case "DECIMAL":
//            case "NUMERIC":
//                return XSD.decimal;
//            case "FLOAT":
//            case "REAL":
//                return XSD.xfloat;
//            case "DOUBLE PRECISION":
//                return XSD.xdouble;
//            case "DATE":
//                return XSD.date;
//            case "TIME":
//                return XSD.time;
//            case "TIMESTAMP":
//                return XSD.dateTimeStamp;
            default:
                return XSD.xstring;
        }
    }

    @Override
    public void generateMetadata(){
        String ds = DataSourceVocabulary.DataSource.getURI() +"/" + name;
        if (!id.equals("")){
            ds = DataSourceVocabulary.DataSource.getURI() +"/" + id;
            G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_ID.getURI(), id);
        }
//
//        G_target.add( ds , RDF.type.getURI(),  DataSourceVocabulary.DataSource.getURI() );
////        G_target.addLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
//        G_target.addLiteral( ds , RDFS.label.getURI(),  name );
//
        G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.SQL.val());

        G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
    }

    public static void main(String[] args) {
        SQLBootstrap_with_DataFrame_MM_without_Jena sql =
                new SQLBootstrap_with_DataFrame_MM_without_Jena(
                        "18",
                        "TaulaEmpleats",
                        "empleats",
                        new PostgresSQLImpl(),//Database type: postgres, mysql...
                        "localhost",
                        "5432",
                        "postgres",
                        "1234", "");
        Graph m = sql.bootstrapSchema(true);
        PrintGraph.printGraph(m);
    }

    @Override
    public BootstrapResult bootstrap(Dataset dataset) {
        Graph bootstrapG = CoreGraphFactory.createGraphInstance("normal");
        String wrapperG;

        SQLBootstrap_with_DataFrame_MM_without_Jena sql =
                new SQLBootstrap_with_DataFrame_MM_without_Jena(
                        dataset.getId(),
                        "odin_test",
                        ((SQLDataset) dataset).getTableName(),
                        new PostgresSQLImpl(),//Database type: postgres, mysql...
                        ((SQLDataset) dataset).getHostname(),
                        ((SQLDataset) dataset).getPort(),
                        ((RelationalJDBCRepository) dataset.getRepository()).getUsername(),
                        ((RelationalJDBCRepository) dataset.getRepository()).getPassword(),
                        "odin_test");
        bootstrapG = sql.bootstrapSchema();
        wrapperG = sql.wrapper;

        return new BootstrapResult(bootstrapG, wrapperG);
    }

    @Override
    public Graph bootstrapGraph(Dataset dataset) {
        Graph bootstrapG = CoreGraphFactory.createGraphInstance("normal");
        String wrapperG;

        SQLBootstrap_with_DataFrame_MM_without_Jena sql =
                new SQLBootstrap_with_DataFrame_MM_without_Jena(
                        dataset.getId(),
                        "odin_test",
                        ((SQLDataset) dataset).getTableName(),
                        new PostgresSQLImpl(),//Database type: postgres, mysql...
                        ((SQLDataset) dataset).getHostname(),
                        ((SQLDataset) dataset).getPort(),
                        ((RelationalJDBCRepository) dataset.getRepository()).getUsername(),
                        ((RelationalJDBCRepository) dataset.getRepository()).getPassword(),
                        "odin_test");
        bootstrapG = sql.bootstrapSchema();
        wrapperG = sql.wrapper;

        return bootstrapG;
    }

}


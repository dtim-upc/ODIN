package edu.upc.essi.dtim.nextiabs.implementations;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataSourceVocabulary;
import edu.upc.essi.dtim.NextiaCore.vocabulary.Formats;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import edu.upc.essi.dtim.nextiabs.bootstrap.IBootstrap;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapODIN;
import edu.upc.essi.dtim.nextiabs.databaseConnection.IDatabaseSystem;
import edu.upc.essi.dtim.nextiabs.databaseConnection.SQLTableData;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import org.apache.jena.atlas.lib.Pair;

import java.util.LinkedList;
import java.util.List;

import static edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS.productionRulesDataframe_to_RDFS;

/**
 * Generates an instance of a DataFrame_Metamodel representation of a postgresSQL database
 * @author juane
 */
public class SQLBootstrap extends DataSource implements IBootstrap<Graph>, BootstrapODIN {
    // Using DataFrame_MM and without Jena
    private final IDatabaseSystem Database;
    private SQLTableData tableData;
    private final String hostname;
    private final String port;
    private final String username;
    private final String password;
    private final String tableName;
    private final String databaseName;

    public SQLBootstrap(String id, String schemaName, String tableName, IDatabaseSystem DBType, String hostname, String port, String username, String password, String databaseName) {
        super();
        this.id = id;
        this.name = schemaName;
        G_target = (LocalGraph) CoreGraphFactory.createGraphInstance("local");
        this.Database = DBType;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
        this.tableData = new SQLTableData(tableName);
        this.databaseName = databaseName;
    }

    @Override
    public Graph bootstrapSchema(Boolean generateMetadata) {
        Database.connect(hostname, port, username, password, databaseName);
        tableData = Database.getMetamodelSingleTable(tableName); // Table's raw data

        productionRules_SQL_to_DF_MM(tableName); // SQLMetamodelTable --> DataFrame_MM

        List<String> columns = new LinkedList<>();
        for (Pair<String, String> col: tableData.getColumns()) {
            columns.add(col.getLeft() + " AS `" + col.getLeft() + "`");
        }
        String columnNames = String.join(", ", columns);
        wrapper = "SELECT " + columnNames + " FROM " + tableName;

        if (generateMetadata) {
            generateMetadata();
        }

        G_target = productionRulesDataframe_to_RDFS(G_target);
        return G_target;
    }

    private void productionRules_SQL_to_DF_MM(String tableName) {
        // Rule 1: Instances of sql:Table are translated to instances of DF_MM:DataFrame.
        G_target.addTriple(createIRI(tableName), RDF.type, DataFrame_MM.DataFrame);
        G_target.addTripleLiteral(createIRI(tableName), RDFS.label, tableName);

        // Rule 2: Instances of sql:Column are translated to instances of DF_MM:Data.
        for (Pair<String, String> col: tableData.getColumns()) {
            G_target.addTriple(createIRI(col.getLeft()), RDF.type, DataFrame_MM.Data);
            G_target.addTripleLiteral(createIRI(col.getLeft()), RDFS.label,col.getLeft());
            G_target.addTriple(createIRI(tableName), DataFrame_MM.hasData,createIRI(col.getLeft()));
            if (col.getRight().equals("integer"))
                G_target.addTriple(createIRI(col.getLeft()),DataFrame_MM.hasDataType, DataFrame_MM.Number );
            else
                G_target.addTriple(createIRI(col.getLeft()),DataFrame_MM.hasDataType, DataFrame_MM.String );
        }
    }

    @Override
    public void generateMetadata(){
        String ds = DataSourceVocabulary.DataSource.getURI() +"/" + name;
        if (!id.isEmpty()) {
            ds = DataSourceVocabulary.DataSource.getURI() +"/" + id;
            G_target.addTripleLiteral(ds , DataSourceVocabulary.HAS_ID.getURI(), id);
        }

//        G_target.add( ds , RDF.type.getURI(),  DataSourceVocabulary.DataSource.getURI() );
//        G_target.addLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
//        G_target.addLiteral( ds , RDFS.label.getURI(),  name );

        G_target.addTripleLiteral(ds , DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.SQL.val());
        G_target.addTripleLiteral(ds , DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
    }

    @Override
    public Graph bootstrapSchema() {
        return bootstrapSchema(false);
    }

    @Override
    public BootstrapResult bootstrapDataset(Dataset dataset) {
        bootstrapSchema();
        return new BootstrapResult(G_target, wrapper);
    }
}

//    private String DBTypeToRDFSType(String type) {
//        switch (type.toUpperCase()) {
//            case "CHAR":
//            case "VARCHAR":
//            case "LONGVARCHAR":
//            case "NCHAR":
//            case "NVARCHAR":
//            case "LONGNVARCHAR":
//            case "INTERVAL DAY-TIME":
//            case "INTERVAL YEAR-MONTH":
//                return XSD.xstring;
////            case "BINARY":
////            case "VARBINARY":
////            case "LONGVARBINARY":
////                return XSD.base64Binary;
////            case "BOOLEAN":
////                return XSD.xboolean;
////            case "SMALLINT":
////                return XSD.xshort;
//            case "INTEGER":
//                return XSD.xint;
////            case "BIGINT":
////                return XSD.xlong;
////            case "DECIMAL":
////            case "NUMERIC":
////                return XSD.decimal;
////            case "FLOAT":
////            case "REAL":
////                return XSD.xfloat;
////            case "DOUBLE PRECISION":
////                return XSD.xdouble;
////            case "DATE":
////                return XSD.date;
////            case "TIME":
////                return XSD.time;
////            case "TIMESTAMP":
////                return XSD.dateTimeStamp;
//            default:
//                return XSD.xstring;
//        }
//    }


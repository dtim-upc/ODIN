package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.odin.query.pojos.Property;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.QueryResult;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class qrModuleImpl implements qrModuleInterface {
    @Override
    public QueryResult makeQuery(IntegratedGraphJenaImpl integratedGraph, List<edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset> integratedDatasets, QueryDataSelection body) {
        QueryResult res = new QueryResult();
        // Code to read the first lines of a ResultSet, just in case
//        ResultSet rs = null; // NextiaQRcall()
//        try {
//            ResultSetMetaData rsmd = rs.getMetaData();
//            // First, get the names of the columns
//            List<String> columns = new LinkedList<>();
//            int columnCount = rsmd.getColumnCount();
//            for (int i = 1; i <= columnCount; i++ ) { // The column count starts from 1
//                columns.add(rsmd.getColumnName(i));
//            }
//            res.setColumns(columns);
//            // Get a sample of the data (first x rows)
//            List<String> rows = new LinkedList<>();
//            int count = 0;
//            while (rs.next() && count < 50) {
//                Map<String, String> adaptedRow = new HashMap<>();
//                for (int i = 1; i <= columnCount; i++) {
//                    adaptedRow.put(rsmd.getColumnName(i), rs.getString(i));
//                }
//                String rowJson = convertMapToJsonString(adaptedRow);
//                rows.add(rowJson);
//                ++count;
//            }
//            res.setRows(rows);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

        // Hardcoded dataset
        // TODO modificar siguiente llamada a la del módulo de la query que debería retornar un Dataset<Row>
//        Dataset<Row> dataFrame = hardcodeDataFrame(body.getProperties());
//
//        // global of the integrated one
//        integratedGraph.getGlobalGraph(); // es pot treure?
//        // local graph
//        integratedDatasets.get(0).getLocalGraph(); // es pot treure?
//
//        res.setColumns(getColumnsFromDataFrame(dataFrame));
//
//        res.setRows(getRowsFromDataFrame(dataFrame));

        String CSVPath = "C:\\Projects\\ODIN\\api\\dbFiles\\DataLayer\\tmp\\titanic.csv"; // NextiaQRcall()
        try (CSVReader csvReader = new CSVReader(new FileReader(CSVPath))) {
            List<String> adaptedRows = new ArrayList<>();
            // Read the header to get the column names
            String[] header = csvReader.readNext();
            List<String> columnNames = Arrays.asList(header);
            res.setColumns(columnNames);

            // Read the remaining lines with values
            String[] line;
            int lineCount = 0;
            while ((line = csvReader.readNext()) != null && lineCount < 20) { // only the first 20
                List<String> values = Arrays.asList(line);
                Map<String, String> adaptedRow = new HashMap<>();
                for (int i = 0; i< values.size(); ++i) {
                    adaptedRow.put(res.getColumns().get(i), values.get(i));
                }
                String rowJson = convertMapToJsonString(adaptedRow);
                adaptedRows.add(rowJson);
                lineCount++;
            }
            res.setRows(adaptedRows);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        res.setCSVPath(CSVPath);
        return res;
    }

    private List<String> getColumnsFromDataFrame(Dataset<Row> dataFrame) {
        return List.of(dataFrame.columns());
    }

    private List<String> getRowsFromDataFrame(Dataset<Row> dataFrame) {
        // Crear una lista para almacenar las filas adaptadas
        List<String> adaptedRows = new ArrayList<>();

        // Iterar sobre las filas del DataFrame
        for (Row row : dataFrame.collectAsList()) {
            Map<String, String> adaptedRow = new HashMap<>();

            // Iterar sobre las columnas del DataFrame
            for (String column : dataFrame.columns()) {
                adaptedRow.put(column, row.getString(row.fieldIndex(column)));
            }

            // Convertir el mapa a una cadena JSON y agregarlo a la lista
            String rowJson = convertMapToJsonString(adaptedRow);
            adaptedRows.add(rowJson);
        }

        return adaptedRows;
    }

    private Dataset<Row> hardcodeDataFrame(List<Property> properties) {
        // Configuración de Spark
        SparkSession spark = SparkSession.builder()
                .appName("SparkExample")
                .master("local[*]")
                .getOrCreate();

        // Crear un conjunto de datos hardcodeado
        // Definir el esquema del DataFrame (las columnas)
        StructType schema = new StructType();

        for (Property property : properties){
            String iri = property.getIri();
            String ultimaParte = iri;
            // Encuentra la última posición del símbolo '#'
            int lastHashIndex = iri.lastIndexOf("/");

            // Comprueba si se encontró el símbolo '#'
            if (lastHashIndex != -1) {
                // Obtiene la parte de la cadena desde la última posición del símbolo '#' hasta el final
                ultimaParte = iri.substring(lastHashIndex + 1);

                // Imprime la última parte
                System.out.println("Última parte de la cadena: " + ultimaParte);
            } else {
                // Si no se encuentra el símbolo '#', imprime un mensaje de error o realiza alguna acción apropiada
                System.out.println("La cadena no contiene el símbolo '/'");
            }
            schema = schema.add(new StructField(ultimaParte, DataTypes.StringType, false, Metadata.empty()));
        }

        List<Row> data = generateDataFromColumnsNum(properties.size());

        // Crear el DataFrame
        return spark.createDataFrame(data, schema);
    }

    private List<Row> generateDataFromColumnsNum(int size) {
        List<Row> data = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            List<Object> values = new ArrayList<>();
            for (int j = 1; j <= size; j++) {
                values.add("Value " + i + " col. " + j);
            }
            data.add(RowFactory.create(values.toArray()));
        }

        return data;
    }

    // Método para convertir un mapa a una cadena JSON
    private String convertMapToJsonString(Map<String, String> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Manejar la excepción según tus necesidades
            return null;
        }
    }
}

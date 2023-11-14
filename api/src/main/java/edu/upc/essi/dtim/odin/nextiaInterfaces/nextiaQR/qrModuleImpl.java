package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.odin.query.pojos.Property;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.*;

public class qrModuleImpl implements qrModuleInterface {
    @Override
    public RDFSResult makeQuery(QueryDataSelection body) {
        // Crear una instancia de RDFSResult
        RDFSResult res = new RDFSResult();
        Dataset<Row> dataFrame = hardcodeDataFrame(body.getProperties());

        // Configurar las columnas
        res.setColumns(List.of(dataFrame.columns()));

        // Configurar las filas
        res.setRows(getRowsFromDataFrame(dataFrame));

        return res;
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
        // Aquí estoy creando un DataFrame con las columnas "Subject", "Predicate", "Object", "DataType"
        List<Row> data = Arrays.asList(
                RowFactory.create("Person1", "hasName", "John Doe", "xsd:string"),
                RowFactory.create("Person2", "hasAge", "25", "xsd:integer"),
                RowFactory.create("Person3", "hasOccupation", "Engineer", "xsd:string"),
                RowFactory.create("Person4", "hasCity", "New York", "xsd:string"),
                RowFactory.create("Person5", "hasAge", "30", "xsd:integer"),
                RowFactory.create("Person6", "hasOccupation", "Doctor", "xsd:string"),
                RowFactory.create("Person7", "hasCity", "San Francisco", "xsd:string"),
                RowFactory.create("Person8", "hasAge", "28", "xsd:integer"),
                RowFactory.create("Person9", "hasOccupation", "Teacher", "xsd:string"),
                RowFactory.create("Person10", "hasCity", "London", "xsd:string")
        );

        // Definir el esquema del DataFrame
        StructType schema = new StructType(new StructField[]{
                new StructField("Subject", DataTypes.StringType, false, Metadata.empty()),
                new StructField("Predicate", DataTypes.StringType, false, Metadata.empty()),
                new StructField("Object", DataTypes.StringType, false, Metadata.empty()),
                new StructField("DataType", DataTypes.StringType, false, Metadata.empty())
        });

        for (Property property : properties){
            schema.add(new StructField("Subject", DataTypes.StringType, false, Metadata.empty()));
        }

        // Crear el DataFrame
        return spark.createDataFrame(data, schema);
    }

    // Método para convertir un mapa a una cadena JSON todo eliminar cuando ya no se hardcodee
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

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

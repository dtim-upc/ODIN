package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import com.opencsv.CSVWriter;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.implementations.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class DataLayerImpl implements DataLayerInterace {
    private final String dataLayerPath;
    private final String technology;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
        this.technology = appConfig.getDataLayerTechnology();
    }

    private DataLayer getDataLayer() {
        DataLayer dl;
        try {
            dl = DataLayerFactory.getInstance(technology, dataLayerPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dl;
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLoading dloading = DataLoadingSingleton.getInstance(dataLayerPath);
        System.out.println(dataset.getWrapper());
        dloading.uploadToLandingZone(dataset);

        DataLayer dl = getDataLayer();
        try {
            dl.uploadToFormattedZone(dataset, dataset.getDataLayerPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void deleteDataset(String dataLayerPath) {
        DataLayer dl = getDataLayer();
        try {
            dl.RemoveFromFormattedZone(dataLayerPath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String reconstructFile(MultipartFile multipartFile, String repositoryIdAndName) {
        DataLoading dloading = DataLoadingSingleton.getInstance(dataLayerPath);
        if (multipartFile.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        String originalFileName = multipartFile.getOriginalFilename();
        // If the file comes from an API call there is no name, so we have to introduce a placeholder (as we will use the UUID in the tables)
        if (originalFileName.isEmpty()) {
            originalFileName = "apiCall.json";
        }
        try {
            return dloading.reconstructFile(multipartFile.getInputStream(), originalFileName, repositoryIdAndName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String reconstructTable(String tableName, String url, String username, String password) {
        System.out.println("ENTRO A RECONSTRUIR LA TALBA "+tableName);
        String jdbcUrl = url;
        String usuario = username;
        String contrasena = password;
        String nombreTabla = tableName;
        String rutaArchivoCSV = dataLayerPath+tableName+".csv";

        try (Connection conexion = DriverManager.getConnection(jdbcUrl, usuario, contrasena);
             Statement statement = conexion.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + nombreTabla);
             CSVWriter csvWriter = new CSVWriter(new FileWriter(rutaArchivoCSV))) {

            // Escribir encabezados
            csvWriter.writeAll(resultSet, true);

            System.out.println("Tabla descargada exitosamente en formato CSV.");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rutaArchivoCSV;

        /* todo uncomment when DL done
        DataLayer dl = getDataLayer();
        try {
            return dl.reconstructTable(datasetName, url, username, password);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         */
    }
}

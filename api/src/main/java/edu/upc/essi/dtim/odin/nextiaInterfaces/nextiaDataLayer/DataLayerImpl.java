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
        if (multipartFile.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        String originalFilename = multipartFile.getOriginalFilename();

        if (originalFilename != null) {
            int lastSlashIndex = originalFilename.lastIndexOf("/");

            if (lastSlashIndex >= 0) {
                originalFilename = originalFilename.substring(lastSlashIndex + 1);
                // extractedSubstring ahora contiene la parte de la cadena después de la última "/"
                System.out.println("Substring extraída: " + originalFilename);
            } else {
                // No se encontró "/" en el nombre de archivo original, por lo que originalFilename no se modifica.
                System.out.println("No se encontró '/' en el nombre de archivo original.");
            }
        }

        String modifiedFilename = repositoryIdAndName + "/" + originalFilename;

        System.out.println(originalFilename);
        System.out.println(modifiedFilename);

        // Get the disk path from the app configuration
        Path diskPath = Path.of(dataLayerPath);

        // Resolve the destination file path using the disk path and the modified filename
        Path destinationFile = diskPath.resolve(modifiedFilename);

        // Create parent directories if they don't exist
        try {
            Files.createDirectories(destinationFile.getParent());

            // Copy the input stream of the multipart file to the destination file
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // Return the absolute path of the stored file
        return destinationFile.toString();

        /* todo uncomment when DL done
        DataLayer dl = getDataLayer();
        try {
            return dl.reconstructFile(multipartFile, repositoryIdAndName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         */
    }

    @Override
    public String reconstructTable(String tableName, String url, String username, String password) {
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

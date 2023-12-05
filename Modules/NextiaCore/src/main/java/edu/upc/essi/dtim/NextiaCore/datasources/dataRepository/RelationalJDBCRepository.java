package edu.upc.essi.dtim.NextiaCore.datasources.dataRepository;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelationalJDBCRepository extends DataRepository{
    String username;
    String password;
    String url;

    public RelationalJDBCRepository() {
    }

    public RelationalJDBCRepository(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean testConnection() {
        try {
            Connection conexion = DriverManager.getConnection(url, username, password);
            conexion.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> retrieveTables(){
        List<String> tableList = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Establecer la conexión a la base de datos
            connection = DriverManager.getConnection(url, username, password);

            // Crear una declaración SQL
            statement = connection.createStatement();

            // Ejecutar la consulta para mostrar las tablas
            resultSet = statement.executeQuery("SELECT table_name\n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_schema = 'public';");

            // Recorrer los resultados y agregar los nombres de las tablas a la lista
            while (resultSet.next()) {
                tableList.add(resultSet.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return tableList;
    }

    public String retrieveHostname() {
        Pattern pattern = Pattern.compile("jdbc:postgresql://([a-zA-Z0-9.-]+):(\\d+)/");
        Matcher matcher = pattern.matcher(this.url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            // Manejar el caso en el que la cadena no coincida con el formato esperado
            return null;
        }
    }

    public String retrievePort() {
        Pattern pattern = Pattern.compile("jdbc:postgresql://([a-zA-Z0-9.-]+):(\\d+)/");
        Matcher matcher = pattern.matcher(this.url);

        if (matcher.find()) {
            return matcher.group(2);
        } else {
            // Manejar el caso en el que la cadena no coincida con el formato esperado
            return null;
        }
    }
}

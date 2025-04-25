package edu.upc.essi.dtim.NextiaCore.repositories;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelationalJDBCRepository extends DataRepository{
    String username;
    String password;
    String url;

    public RelationalJDBCRepository() {}

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
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> retrieveTables(){
        List<String> tableList = new ArrayList<>();
        Connection connection;
        Statement statement;
        ResultSet resultSet;

        try {
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT table_name\n" +
                                                    "FROM information_schema.tables\n" +
                                                    "WHERE table_schema = 'public';");
            while (resultSet.next()) {
                tableList.add(resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return tableList;
    }

    public String retrieveHostname() {
        Pattern pattern = Pattern.compile("jdbc:postgresql://([a-zA-Z0-9.-]+):(\\d+)/");
        Matcher matcher = pattern.matcher(this.url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Could not retrieve hostname from URL");
        }
    }

    public String retrievePort() {
        Pattern pattern = Pattern.compile("jdbc:postgresql://([a-zA-Z0-9.-]+):(\\d+)/");
        Matcher matcher = pattern.matcher(this.url);

        if (matcher.find()) {
            return matcher.group(2);
        } else {
            throw new RuntimeException("Could not retrieve port from URL");
        }
    }
}

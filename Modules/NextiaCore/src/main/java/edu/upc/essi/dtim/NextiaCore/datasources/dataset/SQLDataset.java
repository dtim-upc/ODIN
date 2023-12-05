package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

public class SQLDataset extends Dataset{
    String tableName;
    String hostname;
    String port;
    String username;
    String password;

    public SQLDataset(String id, String name, String description) {
        super(id, name, description);
    }

    public SQLDataset(String id, String name, String description, String tableName, String hostname, String port, String username, String password) {
        super(id, name, description);
        this.hostname = hostname;
        this. tableName = tableName;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public SQLDataset() {
        super();
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
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
}

package store.badger.essentialbot.api;

import java.sql.*;

public class SQLHelper {
    private Connection conn;
    private boolean isConnected = false;
    public SQLHelper(String ip, int port, String dbName, String username, String password) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        conn = DriverManager.getConnection(
                "jdbc:mysql://" + ip + ":" + port + "/" + dbName,username,password);
        this.isConnected = true;
    }

    public SQLHelper() {}

    public Connection getConn() {
        return this.conn;
    }

    public boolean isConnected() {
        try {
            if (!conn.isClosed()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet runQuery(String query) {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean runStatement(String state) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(state);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

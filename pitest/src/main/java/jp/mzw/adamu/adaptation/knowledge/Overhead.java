package jp.mzw.adamu.adaptation.knowledge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Overhead extends KnowledgeBase implements DataBase {

    protected static Overhead instance = null;
    public static Overhead getInstance() {
        if (instance == null) {
            instance = new Overhead();
        }
        return instance;
    }

    private static Connection conn = null;

    @Override
    public Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:logs/latest/overheads.db");
       }
       return conn;
    }

    @Override
    public void init() throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.executeUpdate("drop table if exists overhead");
        stmt.executeUpdate("create table overhead (time integer, source string, overhead string)");
        stmt.close();
    }

    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public static enum Type {
         ARIMA,
         HeuristicForecast,
         Geweke,
         HeuristicSuggestion
    }

    public synchronized void insert(Type type, long time) {
         try {
              Statement stmt = getConnection().createStatement();
              stmt.executeUpdate("insert into overhead values (" + System.currentTimeMillis() + ",'" + type.name() + "','" + time + "')");
              stmt.close();
         } catch (SQLException e) {
              e.printStackTrace();
         }
    }

    @Override
    public void output() {
    }

}

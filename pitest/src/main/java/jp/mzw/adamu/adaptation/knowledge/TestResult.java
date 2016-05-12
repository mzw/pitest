package jp.mzw.adamu.adaptation.knowledge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestResult extends KnowledgeBase implements DataBase {

    protected static TestResult instance = null;
    public static TestResult getInstance() {
        if (instance == null) {
            instance = new TestResult();
        }
        return instance;
    }

    private static Connection conn = null;
    
    @Override
    public Connection getConnection() throws SQLException {
         if (conn == null) {
              conn = DriverManager.getConnection("jdbc:sqlite:logs/latest/test_results.db");
         }
         return conn;
    }
    
    @Override
    public void init() throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.executeUpdate("drop table if exists test_results");
        stmt.executeUpdate("create table test_results (time integer, mutation string, status string)");
        stmt.close();
    }
    
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    public synchronized void insert(String mutantId, String status) {
         try {
              Statement stmt = getConnection().createStatement();
              stmt.executeUpdate("insert into test_results values (" + System.currentTimeMillis() + ",'" + mutantId + "','" + status + "')");
              stmt.close();
         } catch (SQLException e) {
              e.printStackTrace();
         }
    }
    
    @Override
    public void output() {
        
    }
}

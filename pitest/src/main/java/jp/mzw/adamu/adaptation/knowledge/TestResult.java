package jp.mzw.adamu.adaptation.knowledge;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.pitest.mutationtest.DetectionStatus;

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
        try {
            long start = Stats.class.newInstance().getStartTime();
            StringBuilder builder = new StringBuilder();
            String delim = "";
            
            Statement stmt = getConnection().createStatement();
			ResultSet results = stmt.executeQuery("select time, mutation, status from test_results");
            while (results.next()) {
                int time = (int)(results.getInt(1) - start);
	        	String mutation = results.getString(2);
	        	String status = results.getString(3);
                builder.append(delim).append(time).append(COMMA).append(mutation).append(COMMA).append(status);
                delim = BR;
            }
            results.close();
            stmt.close();

            FileUtils.write(new File(Log.getLatestDir(), "test_results.csv"),  builder.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

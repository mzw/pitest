package jp.mzw.adamu.adaptation.knowledge;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;

public class SAMS extends KnowledgeBase implements DataBase {

    protected static SAMS instance = null;
    public static SAMS getInstance() {
        if (instance == null) {
            instance = new SAMS();
        }
        return instance;
    }

    private static Connection conn = null;
    @Override
    public Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:logs/latest/suggested_ams.db");
        }
        return conn;
    }
    
    @Override
    public void init() throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.executeUpdate("drop table if exists ams");
        stmt.executeUpdate("create table ams (time integer, mutant_order integer, score real)");
        stmt.close();
    }
    
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    public synchronized void insert(int order, double score) {
         try {
              Statement stmt = getConnection().createStatement();
              stmt.executeUpdate("insert into ams values (" + System.currentTimeMillis() + "," + order + "," + score + ")");
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
            ResultSet results = stmt.executeQuery("select time, mutant_order, score from ams");
            while (results.next()) {
                int time = (int)(results.getInt(1) - start);
                int order = results.getInt(2);
                double score = results.getDouble(3);
                builder.append(delim).append(time).append(COMMA).append(order).append(COMMA).append(score);
                delim = BR;
            }
            results.close();
            stmt.close();

            FileUtils.write(new File(Log.getLatestDir(), "suggested.ams.csv"),  builder.toString());
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

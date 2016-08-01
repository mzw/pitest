package jp.mzw.adamu.adaptation.knowledge;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.pitest.mutationtest.DetectionStatus;

public class RtMS extends KnowledgeBase implements DataBase {
	
	public static 
    
    int numKilledMutants;
    int numExaminedMutants;
    DetectionStatus status;
    double score;
    
//    public RtMS(int numKilledMutants, int numExaminedMutants, DetectionStatus status) {
//         this.numKilledMutants = numKilledMutants;
//         this.numExaminedMutants = numExaminedMutants;
//         this.status = status;
//         this.score = (double) numKilledMutants / (double) numExaminedMutants;
//    }
//    
//    public int getNumKilledMutants() {
//         return this.numKilledMutants;
//    }
//    
//    public int getNumExaminedMutants() {
//         return this.numExaminedMutants;
//    }
//    
//    public DetectionStatus getStatus() {
//    	return this.status;
//    }
//    
//    public double getScore() {
//         return this.score;
//    }

//    protected RtMS() { /* NOP */ }
    protected static RtMS instance = null;
    public static RtMS getInstance() {
        if (instance == null) {
            instance = new RtMS();
        }
        return instance;
    }

    private static Connection conn = null;
    
    @Override
    public Connection getConnection() throws SQLException {
         if (conn == null) {
              conn = DriverManager.getConnection("jdbc:sqlite:logs/latest/rtms.db");
         }
         return conn;
    }
    
    @Override
    public void init() throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.executeUpdate("drop table if exists rtms");
        stmt.executeUpdate("create table rtms (time integer, score real)");
        stmt.close();
    }
    
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
//    public synchronized void insert(double score) throws SQLException {
//        Statement stmt = getConnection().createStatement();
//        String query = new StringBuilder()
//            .append("insert into rtms values (")
//            .append(System.currentTimeMillis())
//            .append(",")
//            .append(score)
//            .append(")")
//            .toString();
//        stmt.executeUpdate(query);
//        stmt.close();
//    }

    public synchronized double insert(int numExaminedMutants, int numKilledMutants) throws SQLException {
    	double score = KnowledgeBase.getScore(numExaminedMutants, numKilledMutants);
        Statement stmt = getConnection().createStatement();
        String query = new StringBuilder()
            .append("insert into rtms values (")
            .append(System.currentTimeMillis())
            .append(",")
            .append(score)
            .append(")")
            .toString();
        stmt.executeUpdate(query);
        stmt.close();
        return score;
    }
    
//    public double[] getRtmsArray() throws SQLException {
//        Statement stmt = getConnection().createStatement();
//        ResultSet results = stmt.executeQuery("select score from rtms");
//        ArrayList<Double> rtmsList = new ArrayList<Double>();
//        while (results.next()) {
//             double score = results.getDouble(1);
//             rtmsList.add(score);
//        }
//        results.close();
//        stmt.close();
//        double[] rtmsArray = new double[rtmsList.size()];
//        for (int i = 0; i < rtmsList.size(); i++) {
//             rtmsArray[i] = rtmsList.get(i);
//        }
//        return rtmsArray;
//    }

    @Override
    public void output() {
        try {
            long start = Stats.class.newInstance().getStartTime();
            StringBuilder builder = new StringBuilder();
            String delim = "";
            
            Statement stmt = getConnection().createStatement();
            ResultSet results = stmt.executeQuery("select time, score from rtms");
            while (results.next()) {
                int time = (int)(results.getInt(1) - start);
                double score = results.getDouble(2);
                builder.append(delim).append(time).append(COMMA).append(score);
                delim = BR;
            }
            results.close();
            stmt.close();

            FileUtils.write(new File(Log.getLatestDir(), "rtms.csv"),  builder.toString());
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

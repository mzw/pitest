package jp.mzw.adamu.adaptation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import jp.mzw.adamu.adaptation.knowledge.Knowledge;
import jp.mzw.adamu.adaptation.knowledge.Log;
import jp.mzw.adamu.adaptation.knowledge.Stats;

/**
 * Execute function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Executor {
    /**
     * Finalize running AdaMu
     * @throws SQLException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static void execute() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Knowledge.output();
        Knowledge.closeDataBases();
        Log.copyResultsWithTimestamp();
    }
    
    /**
     * Interrupt running PIT when developers decide to accept suggestions from running AdaMu
     * @author Yuta Maezawa
     */
    public static class Interrupter extends Thread {
        /**
         * To possess thread pool in running PIT
         */
        ThreadPoolExecutor executor;
        
        /**
         * To interrupt running PIT from running AdaMu
         * @param executor Thread pool in running PIT
         */
        public Interrupter(ThreadPoolExecutor executor) {
            this.executor = executor;
        }

        /**
         * Wait for make decision from developers or finished AdaMu
         */
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (decideQuit()) {
                        // workaround: running PIT will quit with exception
                        List<Runnable> workers = executor.shutdownNow();
                        for (Runnable worker : workers) {
                            FutureTask<?> task = (FutureTask<?>) worker;
                            task.cancel(true);
                        }
                        if (!Stats.getInstance().getConnection().isClosed()) {
                            Executor.execute();
                        }
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean decideQuit() throws SQLException, InstantiationException, IllegalAccessException {
        Connection conn = Stats.class.newInstance().getConnection();
        if (conn.isClosed()) { // Finished
             return true;
        }
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("select key from stats");
        while (results.next()) {
             String key = results.getString(1);
             if (Stats.Label.Quit.name().equals(key)
                       || Stats.Label.Finish.name().equals(key)) {
                  return true;
             }
        }
        results.close();
        stmt.close();
        return false;
    }
}

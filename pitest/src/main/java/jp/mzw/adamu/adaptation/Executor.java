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
 * 
 * @author Yuta Maezawa
 */
public class Executor {
	/**
	 * Finalize running AdaMu
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void execute() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Knowledge.output();
		Knowledge.closeDataBases();
		Log.copyResultsWithTimestamp();
	}

	/**
	 * Interrupt running PIT when developers decide to accept suggestions from
	 * running AdaMu
	 * 
	 * @author Yuta Maezawa
	 */
	public static class Interrupter extends Thread {
		/**
		 * To possess thread pool in running PIT
		 */
		ThreadPoolExecutor executor;

		/**
		 * To interrupt running PIT from running AdaMu
		 * 
		 * @param executor
		 *            Thread pool in running PIT
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
					// for completing mutation testing to evaluate usefulness of AdaMu
					break;
//					Thread.sleep(1000);
//					Stats.Label label = decideQuit();
//					if (label != null) {
//						if (Stats.Label.Quit.equals(label)) {
//							List<Runnable> workers = executor.shutdownNow();
//							for (Runnable worker : workers) {
//								FutureTask<?> task = (FutureTask<?>) worker;
//								task.cancel(true);
//							}
//						}
//						Executor.execute();
//						break;
//					}
				} catch (Exception e) {
					break;
				}
			}
		}
	}

	public static Stats.Label decideQuit() {
		try {
			Connection conn = Stats.class.newInstance().getConnection();
			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery("select key from stats");
			while (results.next()) {
				String key = results.getString(1);
				if (Stats.Label.Quit.name().equals(key) || Stats.Label.Finish.name().equals(key)) {
					results.close();
					stmt.close();
					return Stats.Label.valueOf(key); 
				}
			}
			results.close();
			stmt.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}
}

package jp.mzw.adamu.adaptation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.mzw.adamu.adaptation.knowledge.KnowledgeBase;
import jp.mzw.adamu.adaptation.knowledge.Mutations;
import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.adaptation.knowledge.TestResults;
import jp.mzw.adamu.adaptation.knowledge.data.Mutation;
import jp.mzw.adamu.adaptation.knowledge.data.TestResult;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.engine.MutationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor function of MAPE-K control loop implemented in AdaMu
 * 
 * @author Yuta Maezawa
 */
public class Monitor extends MAPE {
	private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

	/**
	 * Store a Unix time when PIT starts mutation testing
	 * @throws SQLException is caused when AdaMu fails to store a Unix time into DB
	 */
	public static void startAdaMuLogger() {
		try {
			long time = System.currentTimeMillis();
			Stats.getInstance().insert(Stats.Label.StartTime, time);
			LOGGER.info("Start: {}", time);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Monitor test execution results on each mutant created by PIT
	 * @param mutation a mutants created by PIT
	 * @param status a test execution result on a mutant
	 */
	public static void monitorMutationResult(MutationDetails mutation, DetectionStatus status, boolean run) {
		if (!status.equals(DetectionStatus.NOT_STARTED) && !status.equals(DetectionStatus.STARTED)) {
			TestResults.getInstance().insert(mutation.hashCode(), mutation.getClassName().toString(), mutation.getMethod().name(), mutation.getLineNumber(),
					mutation.getMutator(), status.toString());
			if (run) {
				new MonitorThread().run();
			}
		}
	}

	public static void monitorMutationResult(MutationDetails mutation, DetectionStatus status) {
		monitorMutationResult(mutation, status, true);
	}

	/**
	 * Monitor test execution results on each mutant created by PIT
	 * @param mutations mutants created by PIT
	 * @param status test execution results on mutants
	 */
	public static void monitorMutationsResult(Collection<MutationDetails> mutations, DetectionStatus status) {
		for (MutationDetails mutation : mutations) {
			monitorMutationResult(mutation, status, true);
		}
	}

	public static class MonitorThread extends Thread {
		@Override
		public void run() {
			try {
				measureRuntimeMutationScore();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Measure runtime mutation score (RtMS)
	 * @throws SQLException is caused when AdaMu fails to store an RtMS into DB
	 */
	public static void measureRuntimeMutationScore() throws SQLException {
		// For measuring runtime mutation score (RtMS)
		int numExaminedMutants = 0;
		int numKilledMutants = 0;
		// For analyzing burn-in period, quit timing, and approximate mutation score (Analyzer)
		List<TestResult> test_result_list = new ArrayList<>();

		// Read from DB
		Statement stmt = TestResults.getInstance().getConnection().createStatement();
		ResultSet results = stmt.executeQuery("select hashcode, class_name, method_name, lineno, mutator, status from test_results");
		while (results.next()) {
			// Data from DB
			int hashcode = results.getInt(1);
			String class_name = results.getString(2);
			String method_name = results.getString(3);
			int lineno = results.getInt(4);
			String mutator = results.getString(5);
			// For RtMS
			numExaminedMutants += 1;
			DetectionStatus status = DetectionStatus.valueOf(results.getString(6));
			if (KnowledgeBase.isKilled(status)) {
				numKilledMutants += 1;
			}
			// For AdaMu Analyzer
			test_result_list.add(new TestResult(hashcode, class_name, method_name, lineno, mutator, status));
		}
		// RtMS
		double rtms = RtMS.getInstance().insert(numExaminedMutants, numKilledMutants);
		LOGGER.info("Runtime mutation score: {} @ {}", rtms, numExaminedMutants);
		// Analyzer
		Analyzer.analyze(test_result_list, getMutationList());
	}

	protected static List<Mutation> mutationList = null;

	public static List<Mutation> getMutationList() throws SQLException {
		if (mutationList == null) {
			mutationList = Mutations.getInstance().getMutations();
		}
		return mutationList;
	}

}

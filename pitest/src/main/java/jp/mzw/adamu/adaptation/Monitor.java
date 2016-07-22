package jp.mzw.adamu.adaptation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.adaptation.knowledge.TestResult;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationGrouper;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor function of MAPE-K control loop implemented in AdaMu
 * 
 * @author Yuta Maezawa
 */
public class Monitor {
	static Logger logger = LoggerFactory.getLogger(Monitor.class);

	/**
	 * Incrementally count the number of available mutants
	 * 
	 * @param availableMutations
	 * @throws SQLException
	 */
	public static void getAailableMutations(Collection<MutationDetails> availableMutations) {
		try {
			int num = availableMutations.size();
			Stats.getInstance().insertNumMutants(num);
			logger.info("Available mutations: {}", num);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Store start time in Unix time
	 * 
	 * @throws SQLException
	 */
	public static void startAdaMuLogger() {
		try {
			long time = System.currentTimeMillis();
			Stats.getInstance().insert(Stats.Label.StartTime, time);
			logger.info("Start: {}", time);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Control an order of test executions on generated mutants which is based
	 * on methods being mutated
	 * 
	 * @param codeClasses
	 * @param mutations
	 * @param grouper
	 * @param workerFactory
	 * @return
	 */
	public static List<MutationAnalysisUnit> orderTestExecutionOnMutants(final Collection<ClassName> codeClasses, final Collection<MutationDetails> mutations,
			final MutationGrouper grouper, WorkerFactory workerFactory) {
		long start = System.currentTimeMillis();
		List<MutationAnalysisUnit> ret = new ArrayList<MutationAnalysisUnit>();

		Map<String, List<MutationDetails>> method_mutation_map = new HashMap<>();
		for (MutationDetails mutation : mutations) {
			String methodName = mutation.getClassName() + "#" + mutation.getMethod();
			List<MutationDetails> mutation_list = method_mutation_map.get(methodName);
			if (mutation_list == null) {
				mutation_list = new ArrayList<>();
			}
			mutation_list.add(mutation);
			method_mutation_map.put(methodName, mutation_list);
		}
		for (String methodName : method_mutation_map.keySet()) {
			List<MutationDetails> mutation_list = method_mutation_map.get(methodName);
			Collections.shuffle(mutation_list);
		}
		boolean remain = true;
		do {
			Collection<MutationDetails> method_based_mutation_list = new ArrayList<>();
			remain = false;
			for (String methodName : method_mutation_map.keySet()) {
				List<MutationDetails> mutation_list = method_mutation_map.get(methodName);
				if (0 < mutation_list.size()) {
					MutationDetails mutation = mutation_list.remove(0);
					method_based_mutation_list.add(mutation);
				}
				if (!mutation_list.isEmpty()) {
					remain = true;
				}
			}
			final Set<ClassName> uniqueTestClasses = new HashSet<ClassName>();
			FCollection.flatMapTo(method_based_mutation_list, MutationTestBuilder.mutationDetailsToTestClass(), uniqueTestClasses);
			MutationTestUnit mtu = new MutationTestUnit(method_based_mutation_list, uniqueTestClasses, workerFactory);
			ret.add(mtu);
		} while (remain);
		long end = System.currentTimeMillis();
		Overhead.getInstance().insert(Overhead.Type.TestExecOrder, end - start);

		return ret;
	}

	public static void monitorMutationResult(MutationIdentifier mutationId, MutationStatusTestPair result) {
		StringBuilder builder = new StringBuilder();
		builder.append(mutationId.getClassName()).append("#").append(mutationId.getLocation().getMethodName()).append(":").append("lineno").append("<")
				.append(mutationId.getMutator());
		TestResult.getInstance().insert(builder.toString(), result.getStatus().toString());
		try {
			measureRuntimeMutationScore();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void monitorMutationsResult(Collection<MutationDetails> mutations, DetectionStatus status) {
		if (!status.equals(DetectionStatus.NOT_STARTED) && !status.equals(DetectionStatus.STARTED)) {
			for (MutationDetails mutation : mutations) {
				StringBuilder builder = new StringBuilder();
				builder.append(mutation.getClassName()).append("#").append(mutation.getMethod()).append(":").append(mutation.getLineNumber()).append("<")
						.append(mutation.getMutator());
				TestResult.getInstance().insert(builder.toString(), status.toString());
				try {
					measureRuntimeMutationScore();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void measureRuntimeMutationScore() throws SQLException, InstantiationException, IllegalAccessException {
		List<RtMS> rtmsList = new ArrayList<>();
		RtMS curRtms = null;

		int numExaminedMutants = 0;
		int numKilledMutants = 0;
		Statement stmt = TestResult.getInstance().getConnection().createStatement();
		ResultSet results = stmt.executeQuery("select status from test_results");
		while (results.next()) {
			numExaminedMutants += 1;
			String status = results.getString(1);
			if (status.equals(DetectionStatus.KILLED.name()) || status.equals(DetectionStatus.MEMORY_ERROR.name())
					|| status.equals(DetectionStatus.TIMED_OUT.name()) || status.equals(DetectionStatus.RUN_ERROR.name())) {
				numKilledMutants += 1;
			}
			curRtms = new RtMS(numKilledMutants, numExaminedMutants, DetectionStatus.valueOf(status));
			rtmsList.add(curRtms);
		}
		RtMS rtms = new RtMS(numKilledMutants, numExaminedMutants, null);
		RtMS.getInstance().insert(rtms.getScore());
		logger.info("Runtime mutation score: {} @ {}", rtms.getScore(), numExaminedMutants);
		// Analyzer.analyzeApproximateMutationScore(rtms);
		Analyzer.analyze(rtmsList, curRtms);
	}

}

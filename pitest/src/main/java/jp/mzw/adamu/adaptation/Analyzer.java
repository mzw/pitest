package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.mzw.adamu.adaptation.knowledge.KnowledgeBase;
import jp.mzw.adamu.adaptation.knowledge.Mutations;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.adaptation.knowledge.data.Mutation;
import jp.mzw.adamu.adaptation.knowledge.data.TestResult;
import jp.mzw.adamu.adaptation.model.ConvergeDiagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyze function of MAPE-K control loop implemented in AdaMu
 * 
 * @author Yuta Maezawa
 */
public class Analyzer {
	static Logger logger = LoggerFactory.getLogger(Analyzer.class);

	private static int num_mutants_burnin = -1;
	
	/**
	 * Analyze to determine whether RtMS is in burn-in period
	 * @param testResultList
	 * @throws SQLException
	 */
	public static void analyze(List<TestResult> testResultList) throws SQLException {
		List<Mutation> mutationList = Mutations.getInstance().getMutations();
		int num_total_mutants = mutationList.size();
		int num_examined_mutants = testResultList.size();
		
		// Already being out of burn-in period
		if (num_mutants_burnin < 0) {
			num_mutants_burnin = Stats.getInstance().getNumMutantsBurnin();
		}
		if (0 < num_mutants_burnin && num_mutants_burnin <= num_examined_mutants) {
			Planner.plan(testResultList, mutationList, num_mutants_burnin);
			return;
		}
		
		// for making faster
		if (skip(num_examined_mutants, num_total_mutants)) {
			logger.info("Skip by interval design... #Examined: {}", num_examined_mutants);
			return;
		}
		if (num_examined_mutants < ConvergeDiagnostic.getNMin(num_total_mutants)) {
			logger.info("Burn-in period...");
			return;
		}
		
		// Converge diagnostic
		int _num_examined_mutants = 0;
		int _num_killed_mutants = 0;
		List<Double> cur_rtms_list = new ArrayList<>();
		for (TestResult test_result : testResultList) {
			++_num_examined_mutants;
			if (KnowledgeBase.isKilled(test_result.getStatus())) {
				++_num_killed_mutants;
			}
			double rtms = KnowledgeBase.getScore(_num_examined_mutants, _num_killed_mutants);
			cur_rtms_list.add(rtms);

			if (_num_examined_mutants == num_examined_mutants) {
				long start = System.currentTimeMillis();
				boolean converge = ConvergeDiagnostic.converge(cur_rtms_list, num_total_mutants);
				long end = System.currentTimeMillis();
				Overhead.getInstance().insert(Overhead.Type.Converge, end - start);

				if (converge) {
					num_mutants_burnin = _num_examined_mutants;
					Stats.getInstance().insert(Stats.Label.Burnin, num_mutants_burnin);
				} else {
					logger.info("Burn-in period...");
				}
			}
		}
		
	}
	
	/**
	 * The percentage of the number of created mutants for skipping to call the analyze function
	 */
	public static final double SKIP_INTERVAL = 0.01; // 1%
	
	/**
	 * Skip to call the analyze function for mitigating computational overhead
	 * @param n the number fo examined mutants so far
	 * @param N
	 * @return
	 */
	public static boolean skip(int n, int N) {
		int interval = (int) Math.ceil(N * SKIP_INTERVAL);
		if (n % interval == 0) {
			return false;
		}
		return true;
	}
	
}

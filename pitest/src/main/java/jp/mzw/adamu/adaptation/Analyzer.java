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
import jp.mzw.adamu.adaptation.model.StoppingRule;

import org.pitest.mutationtest.DetectionStatus;
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
	private static int num_mutants_stop = -1;
	
	public static void analyze(List<TestResult> testResultList) throws SQLException {
		List<Mutation> mutationList = Mutations.getInstance().getMutations();
		int num_total_mutants = mutationList.size();
		int num_examined_mutants = testResultList.size();

		// for making faster
		if (num_examined_mutants < ConvergeDiagnostic.getNMin(num_total_mutants)) {
			logger.info("Burn-in period...");
			return;
		}
		
		if (skip(num_examined_mutants, num_total_mutants)) {
			logger.info("Skip by interval design... #Examined: {}", num_examined_mutants);
			return;
		}
		
		long converge_overhead = 0;
		long stop_overhead = 0;

		boolean burnin = true;
		boolean stop = false;
		List<Double> cur_rtms_list = new ArrayList<>();
		List<Double> cur_useful_rtms_list = new ArrayList<>();
		List<DetectionStatus> cur_useful_status_list = new ArrayList<>();
		
		if (num_mutants_burnin < 0) {
			num_mutants_burnin = Stats.getInstance().getNumMutantsBurnin();
		}
		if (0 < num_mutants_burnin && num_mutants_stop < 0) {
			num_mutants_stop = Stats.getInstance().getNumMutantsStop();
		}
		
		int _num_examined_mutants = 0;
		int _num_killed_mutants = 0;
		for (TestResult test_result : testResultList) {
			++_num_examined_mutants;
			if (KnowledgeBase.isKilled(test_result.getStatus())) {
				++_num_killed_mutants;
			}
			double rtms = KnowledgeBase.getScore(_num_examined_mutants, _num_killed_mutants);
			cur_rtms_list.add(rtms);
			
			// because might be garbage-collected...
			if (num_mutants_burnin < 0) {
				long start = System.currentTimeMillis();
				boolean converge = ConvergeDiagnostic.converge(cur_rtms_list, num_total_mutants);
				long end = System.currentTimeMillis();
				converge_overhead += end - start;
				if (converge) {
					burnin = false;
					num_mutants_burnin = _num_examined_mutants;
					Stats.getInstance().insert(Stats.Label.Burnin, _num_examined_mutants);
					// go to quit suggestion
				} else {
					burnin = true;
					continue;
				}
			} else if (_num_examined_mutants < num_mutants_burnin) {
				burnin = true;
				continue;
			} else if (num_mutants_burnin <= _num_examined_mutants) {
				burnin = false;
				// go to quit suggestion
			}
			
			if (num_mutants_stop < 0) {
				cur_useful_rtms_list.add(rtms);
				cur_useful_status_list.add(test_result.getStatus());

				long start = System.currentTimeMillis();
				boolean stop_decision = StoppingRule.stop(cur_useful_status_list, _num_examined_mutants, num_total_mutants);
				long end = System.currentTimeMillis();
				stop_overhead += end - start;
				if (stop_decision) {
					stop = true;
					num_mutants_stop = _num_examined_mutants;
					Stats.getInstance().insert(Stats.Label.Quit, _num_examined_mutants);
					continue;
				} else {
					stop = false;
					continue;
				}
			} else if (_num_examined_mutants < num_mutants_stop) {
				stop = false;
				cur_useful_rtms_list.add(rtms);
				cur_useful_status_list.add(test_result.getStatus());
				continue;
			} else if (num_mutants_stop <= _num_examined_mutants) {
				stop = true;
				cur_useful_rtms_list.add(rtms);
				cur_useful_status_list.add(test_result.getStatus());
				continue;
			}
			
		}

		// Store information
		Overhead.getInstance().insert(Overhead.Type.Converge, converge_overhead);
		Overhead.getInstance().insert(Overhead.Type.StopDecision, stop_overhead);

		if (burnin) {
			logger.info("Burn-in period...");
		} else if (!stop) {
			logger.info("Continue to measure for making stop decision...#Burn-in: {}", num_mutants_burnin);
		} else {
			logger.info("Suggest for stopping mutation testing...#Burn-in: {} and #Quit: {}", num_mutants_burnin, num_mutants_stop);
			Planner.plan(testResultList, mutationList);
		}
		
	}
	
	/**
	 * The percentage of the number of created mutants for skipping to call the analyze function
	 */
	public static final double SKIP_INTERVAL = 0.005; // 0.5%
	
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

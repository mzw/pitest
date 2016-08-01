package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.mzw.adamu.adaptation.knowledge.KnowledgeBase;
import jp.mzw.adamu.adaptation.knowledge.Mutations;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
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
		
		long converge_overhead = 0;
		long stop_overhead = 0;

		boolean burnin = true;
		boolean stop = false;
		List<Double> cur_rtms_list = new ArrayList<>();
		List<Double> cur_useful_rtms_list = new ArrayList<>();
		List<DetectionStatus> cur_useful_status_list = new ArrayList<>();
		
		int _num_examined_mutants = 0;
		int _num_killed_mutants = 0;
		for (TestResult test_result : testResultList) {
			++_num_examined_mutants;
			if (KnowledgeBase.isKilled(test_result.getStatus())) {
				++_num_killed_mutants;
			}
			double rtms = KnowledgeBase.getScore(_num_examined_mutants, _num_killed_mutants);
			
			
			cur_rtms_list.add(rtms);

			// already calculated
			// but might be garbage-collected...
			if (0 < num_mutants_burnin && 0 < num_mutants_stop) {
				if (_num_examined_mutants < num_mutants_burnin) {
					burnin = true;
				} else if (num_mutants_burnin <= _num_examined_mutants) {
					burnin = false;
					cur_useful_rtms_list.add(rtms);
					if (_num_examined_mutants < num_mutants_stop) {
						stop = false;
					} else if (num_mutants_stop <= _num_examined_mutants) {
						stop = true;
					}
				}
				continue;
			}

			if (burnin) {
				long start = System.currentTimeMillis();
				boolean converge = ConvergeDiagnostic.converge(cur_rtms_list, num_total_mutants);
				long end = System.currentTimeMillis();
				converge_overhead += end - start;
				if (converge) {
					burnin = false;
					num_mutants_burnin = _num_examined_mutants;
				} else {
					// Burn-in period
				}
			} else {
				cur_useful_rtms_list.add(rtms);
				cur_useful_status_list.add(test_result.getStatus());

				if (stop) {
					// Forecasting approximate mutation score
				} else {
					long start = System.currentTimeMillis();
					boolean stop_decision = StoppingRule.stop(cur_useful_status_list, _num_examined_mutants, num_total_mutants);
					long end = System.currentTimeMillis();
					stop_overhead += end - start;
					if (stop_decision) {
						stop = true;
						num_mutants_stop = _num_examined_mutants;
					} else {
						// Continue to measure for making stop decision
					}
				}
			}
		}

		Overhead.getInstance().insert(Overhead.Type.Converge, converge_overhead);
		Overhead.getInstance().insert(Overhead.Type.StopDecision, stop_overhead);

		if (burnin) {
			logger.info("Burn-in period...");
		} else if (!stop) {
			logger.info("Continue to measure for making stop decision...");
		} else {
			logger.info("Suggest for stopping mutation testing");
			Planner.plan(testResultList, mutationList);
		}
		
	}
	
}

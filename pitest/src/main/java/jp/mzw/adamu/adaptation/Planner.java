package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.pitest.mutationtest.DetectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.adaptation.knowledge.data.Mutation;
import jp.mzw.adamu.adaptation.knowledge.data.TestResult;
import jp.mzw.adamu.adaptation.model.StoppingRule;

/**
 * Plan function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Planner {
    static Logger logger = LoggerFactory.getLogger(Planner.class);

	private static int num_mutants_stop = -1;
	
    /**
     * Plan to quit running mutation testing
     * @param testResultList a list of test execution results on each mutant so far
     * @param mutationList a list of created mutants
     * @throws SQLException is caused when AdaMu fails to store forecasting overhead into DB
     */
    public static void plan(List<TestResult> testResultList, List<Mutation> mutationList, int numMutantsBurnin) throws SQLException {

		if (num_mutants_stop < 0) {
			num_mutants_stop = Stats.getInstance().getNumMutantsStop();
		}

		int _num_examined_mutants = 0;
		List<DetectionStatus> cur_useful_status_list = new ArrayList<>();
		for (TestResult test_result : testResultList) {
			++_num_examined_mutants;
			if (_num_examined_mutants <= numMutantsBurnin) {
				continue;
			}
			cur_useful_status_list.add(test_result.getStatus());
			
			if (_num_examined_mutants == testResultList.size()) {
				long start = System.currentTimeMillis();
				boolean stop_decision = StoppingRule.stop(cur_useful_status_list, _num_examined_mutants, mutationList.size());
				long end = System.currentTimeMillis();
				Overhead.getInstance().insert(Overhead.Type.StopDecision, end - start);
				
				if (stop_decision) {
					if (num_mutants_stop < 0) {
						num_mutants_stop = _num_examined_mutants;
						Stats.getInstance().insert(Stats.Label.Quit, num_mutants_stop);
						logger.info("Suggest for stopping mutation testing... #Quit: {}", num_mutants_stop);
					}
					Executor.execute(testResultList, mutationList);
				} else {
					logger.info("Continue to measure for making stop decision...");
				}
			}
		}
    }
    
}

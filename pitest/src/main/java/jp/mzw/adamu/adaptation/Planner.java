package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.KnowledgeBase;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.data.Mutation;
import jp.mzw.adamu.adaptation.knowledge.data.TestResult;

/**
 * Plan function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Planner {
    static Logger logger = LoggerFactory.getLogger(Planner.class);
    
    /**
     * Plan to interrupt PIT mutation testing
     * @param testResultList a list of test execution results on each mutant so far
     * @param mutationList a list of created mutants
     * @throws SQLException is caused when AdaMu fails to store AMS into DB
     */
    public static void plan(List<TestResult> testResultList, List<Mutation> mutationList) throws SQLException {
    	long start = System.currentTimeMillis();
    	
    	// forecast
    	double ams = forecastAms(testResultList, mutationList);
    	// validate
		int num_examined_mutants = testResultList.size();
		int num_killed_mutants = getNumKilledMutants(testResultList);
		int num_total_mutants = mutationList.size();
		boolean valid = isValid(ams, num_examined_mutants, num_killed_mutants, num_total_mutants);

    	long end = System.currentTimeMillis();
		Overhead.getInstance().insert(Overhead.Type.Forecast, end - start);
		
		if (valid) {
			AMS.getInstance().insert(num_examined_mutants, ams);
            logger.info("Approximate mutation score: {} @ {}", ams, num_examined_mutants);
            
            // Intentionally comment-out to complete mutation testing for evaluating usefulness of AdaMu
//			try {
//				Executor.decideQuit();
//			} catch (InstantiationException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
		}

    }
    
    /**
     * Get the number of killed mutants so far
     * @param testResultList a list of test execution results on each mutant so far
     * @return the number of killed mutants so far
     */
    public static int getNumKilledMutants(List<TestResult> testResultList) {
		int num_killed_mutants = 0;
		for (TestResult test_result : testResultList) {
			if (KnowledgeBase.isKilled(test_result.getStatus())) {
				num_killed_mutants++;
			}
		}
		return num_killed_mutants;
    }

    /**
     * Validates an approximate mutation score (AMS)
     * @param ams an AMS
     * @param numExaminedMutants the number of examined mutants so far
     * @param numKilledMutants the number of killed mutants so far
     * @param numTotalMutants the number of created mutants
     * @return true if an AMS is in a valid score range
     */
    public static boolean isValid(double ams, int numExaminedMutants, int numKilledMutants, int numTotalMutants) {
        int numRemainingMutants = numTotalMutants - numExaminedMutants;
        int numMaxKilledMutants = numKilledMutants + numRemainingMutants;
        double maxMutationScore = (double) numMaxKilledMutants / (double) numTotalMutants;
        double minMutationScore = (double) numKilledMutants / (double) numTotalMutants;
        if (minMutationScore <= ams && ams <= maxMutationScore) {
            return true;
        }
        return false;
    }
    
    /**
     * Forecasts an approximate mutation score (AMS)
     * @param testResultList a list of test execution results on each mutant so far
     * @param mutationList a list of created mutants
     * @return an AMS
     */
    private static double forecastAms(List<TestResult> testResultList, List<Mutation> mutationList) {
    	// collect facts
		Map<String, List<TestResult>> method_result_map = new HashMap<>();
		double num_killed_mutants = 0;
		List<Integer> examined_mutants_hashcode_list = new ArrayList<>();
		for (TestResult test_result : testResultList) {
			String key = getClassMethodKey(test_result);
			List<TestResult> value = method_result_map.get(key);
			if (value == null) {
				value = new ArrayList<>();
			}
			value.add(test_result);
			method_result_map.put(key, value);
			examined_mutants_hashcode_list.add(test_result.getHashcode());
			if (KnowledgeBase.isKilled(test_result.getStatus())) {
				num_killed_mutants += 1.0;
			}
		}
		// forecast
		for (Mutation mutation : mutationList) {
			if (!examined_mutants_hashcode_list.contains(mutation.getHashcode())) {
				String key = getClassMethodKey(mutation);
				List<TestResult> corr_examined_mutants = method_result_map.get(key);
				
				int num_corr_killed_mutants = 0;
				int num_corr_examined_mutants = 0;
				for (TestResult test_result : corr_examined_mutants) {
					num_corr_examined_mutants++;
					if (KnowledgeBase.isKilled(test_result.getStatus())) {
						num_corr_killed_mutants++;
					}
				}
				double forecasted_score = (double) num_corr_killed_mutants / (double) (num_corr_examined_mutants);
				num_killed_mutants += 1.0 * forecasted_score;
			}
		}
		// approximate mutation score
		return num_killed_mutants / (double) mutationList.size();
    }
    
    /**
     * Get a key string based on names of class and method where PIT applies a mutation operator
     * @param mutation a mutants created by PIT
     * @return a key string
     */
    private static String getClassMethodKey(Mutation mutation) {
    	return mutation.getClassName() + "#" + mutation.getMethodName();
    }
    
}

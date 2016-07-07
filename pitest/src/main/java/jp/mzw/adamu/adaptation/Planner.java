package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.adaptation.knowledge.SAMS;
import jp.mzw.adamu.adaptation.model.Damp;
import jp.mzw.adamu.adaptation.model.Parabola;
import jp.mzw.adamu.scale.Scale;

/**
 * Plan function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Planner {
    static Logger logger = LoggerFactory.getLogger(Planner.class);
    
    public static void forecastAms(List<RtMS> usefulRtmsList, RtMS curRtms, int N) throws SQLException {
		Double ams = null;
		
		// Manipulating time series data
		DescriptiveStatistics useful_rtms_stats = new DescriptiveStatistics();
		List<Double> rtms_ave_List = new ArrayList<>();
		double score_sum = 0;
		for (int i = 0; i < usefulRtmsList.size(); i++) {
			RtMS rtms = usefulRtmsList.get(i);
			
			double score = rtms.getScore();
			useful_rtms_stats.addValue(score);
			
			score_sum += score;
			double score_ave = score_sum / (i + 1);
			rtms_ave_List.add(score_ave);
		}

		// if examined more than 10K mutants, current RtMS might be actual MS
		if (10000 < usefulRtmsList.size()) {
			ams = curRtms.getScore();
		}
		// Otherwise, forecasting
		else {
			// Fitting
			double grad = Damp.getInitialGrad(rtms_ave_List, N);
			Pair<Double, Double> rate_diff_pair_for_parabola = Parabola.getAccelerationRate(rtms_ave_List, N, grad * -1);
			double rate_parabola = rate_diff_pair_for_parabola.getKey();
			double error_for_parabola = rate_diff_pair_for_parabola.getValue();
			Pair<Double, Double> rate_diff_pair_for_damp = Damp.getAccelerationRate(rtms_ave_List, N, grad * -1);
			double rate_for_damp = rate_diff_pair_for_damp.getKey();
			double error_for_damp = rate_diff_pair_for_damp.getValue();

			// Forecast AMS
			double ams_parabola = Parabola.forecast(rtms_ave_List, curRtms.getNumExaminedMutants(), N, grad, rate_parabola);
			double ams_damp = Damp.forecast(rtms_ave_List, curRtms.getNumExaminedMutants(), N, grad, rate_for_damp);

			// Validate AMS
			boolean valid_ams_parabola = true;
			if (!isValid(ams_parabola, curRtms.getNumExaminedMutants(), curRtms.getNumKilledmutants(), N)) {
				valid_ams_parabola = false;
			}
			boolean valid_ams_damp = true;
			if (!isValid(ams_damp, curRtms.getNumExaminedMutants(), curRtms.getNumKilledmutants(), N)) {
				valid_ams_damp = false;
			}
			
			// If large number of mutants to be examined,
			// parabola model might answer AMS with large error
			// therefore, apply only damp model
			if (10000 < N - curRtms.getNumExaminedMutants()) {
				if (valid_ams_damp) {
					ams = ams_damp;
				} else {
					return;
				}
			}
			
			// If useful data has small deviation AND fitting error is small
			// damp might be superior to parabola
			// because the farmer might answer AMS being closer to current RtMS 
			else if (useful_rtms_stats.getStandardDeviation() < 0.05 && error_for_parabola < 0.01 && error_for_damp < 0.01) {
				if (valid_ams_damp && error_for_damp <= error_for_parabola) {
					ams = ams_damp;
				}
				else if (valid_ams_parabola && error_for_parabola <= error_for_damp) {
					ams = ams_parabola;
				}
				else {
					return;
				}
			}
			// Otherwise, useful data might not be enough to represent actual MS
			// therefore, parabola might be superior to damp
			// because the farmer might answer AMS being to more distant from current RtMS 
			else {
				if (valid_ams_parabola && error_for_parabola <= error_for_damp) {
					ams = ams_parabola;
				}
				else if (valid_ams_damp && error_for_damp <= error_for_parabola) {
					ams = ams_damp;
				}
				else {
					return;
				}
			}
		}
		
		if (ams != null) {
			AMS.getInstance().insert(curRtms.getNumExaminedMutants(), ams);
//			Executor.decideQuit();
		}
    }

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
    
    //--------------------------------------------------
    public static void quitSuggestion(int numExaminedMutants, double score, Scale scale) throws InstantiationException, IllegalAccessException, SQLException {
        AMS amsArrayPair = AMS.getInstance().getAmsArray();
        int[] timeArray = amsArrayPair.getTimeArray();
        double[] amsArray = amsArrayPair.getScoreArray();
        
        long start = System.currentTimeMillis();
        boolean shouldSuggest = Planner.shouldSuggestApproximateMutationScore(timeArray, amsArray, scale);
        long end = System.currentTimeMillis();
        Overhead.getInstance().insert(Overhead.Type.Converge, end - start);
        
        if (shouldSuggest) {
             SAMS.getInstance().insert(numExaminedMutants, score);
        }
    }
     

    public static boolean shouldSuggestApproximateMutationScore(int[] timeArray, double[] scoreArray, Scale scale) {
        int observe = (int) scale.getObserveTime() * 1000;
        int curtime = timeArray[timeArray.length - 1];
        
        ArrayList<Double> valid = new ArrayList<Double>();
        for (int i = 0; i < timeArray.length - 1; i++) {
            int observee = observe + timeArray[i];
            if (curtime < observee) {
                valid.add(scoreArray[i]);
            }
        }
        if (valid.size() < scale.getObserveNumMutants()) {
            logger.info("Suggest: no because valid size is not enough {}", valid.size());
            return false;
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Double score : valid) {
            stats.addValue(score);
        }
        
        double score = scoreArray[scoreArray.length - 1];
        double mean = stats.getMean();
        double sd = stats.getStandardDeviation();
        logger.info("Mean and StdDev: {}, {}", mean, sd);
          
        if (sd < scale.getThresholdStdDev()) {
            if (mean - (sd * 3) < score && score < mean + ( sd * 3)) {
                // 99.73 %, i.e., this score is not an outlier
                logger.info("Suggest: yes");
                return true;
            } else {
                logger.info("Suggest: no becuase AMS is an outlier");
            }
        } else {
            logger.info("Suggest: no bacuase standard deviation is too large");
        }
        
        // Otherwise return false
        return false;
    }
}

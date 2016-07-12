package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.adaptation.knowledge.SAMS;
import jp.mzw.adamu.adaptation.model.forecast.Damp;
import jp.mzw.adamu.adaptation.model.forecast.Forecast;
import jp.mzw.adamu.adaptation.model.forecast.Parabola;
import jp.mzw.adamu.scale.Scale;

/**
 * Plan function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Planner {
    static Logger logger = LoggerFactory.getLogger(Planner.class);
    
    public static void forecastAms(List<RtMS> usefulRtmsList, RtMS curRtms, int N) throws SQLException {
		
		// Manipulating time series data
		DescriptiveStatistics useful_rtms_stats = new DescriptiveStatistics();
		List<Double> useful_rtms_ave_list = new ArrayList<>();
		double score_sum = 0;
		for (int i = 0; i < usefulRtmsList.size(); i++) {
			RtMS rtms = usefulRtmsList.get(i);
			
			double score = rtms.getScore();
			useful_rtms_stats.addValue(score);
			
			score_sum += score;
			double score_ave = score_sum / (i + 1);
			useful_rtms_ave_list.add(score_ave);
		}

		int num_examined_mutants = curRtms.getNumExaminedMutants();
		int num_killed_mutants = curRtms.getNumKilledMutants();
		int num_total_mutants = N;

		Double ams = null;
		// if examined more than 10K mutants, current RtMS might be actual MS
		if (10000 < usefulRtmsList.size()) {
			ams = curRtms.getScore();
		}
		// If large number of mutants remaining,
		// Parabola model might answer AMS with large error
		// therefore, apply only damp model
		else if (10000 < num_total_mutants - num_examined_mutants) {
			double grad = Forecast.getInitialGrad(useful_rtms_ave_list, num_total_mutants);
			Damp damp = new Damp();
			Pair<Double, Double> rate_diff_pair_for_damp = damp.getAccelerationRate(useful_rtms_ave_list, num_total_mutants, grad * -1);
			double rate_for_damp = rate_diff_pair_for_damp.getKey();
			double error_for_damp = rate_diff_pair_for_damp.getValue();
			Map<Integer, Double> forecasts = damp.forecast(useful_rtms_ave_list, num_examined_mutants, num_total_mutants, grad, rate_for_damp);
			double ams_damp = Forecast.getFinal(forecasts);
			if (Forecast.isValid(ams_damp, num_examined_mutants, num_killed_mutants, num_total_mutants)) {
				ams = ams_damp;
			}
		}
		// Otherwise, forecasting
		else {
			// Too small size of time series data for forecasting
			if (useful_rtms_ave_list.size() < N * 0.01) {
				return;
			}
			
			// Fitting
			double grad = Forecast.getInitialGrad(useful_rtms_ave_list, num_total_mutants);
			// Damp
			Damp damp = new Damp();
			Pair<Double, Double> rate_diff_pair_for_damp = damp.getAccelerationRate(useful_rtms_ave_list, num_total_mutants, grad * -1);
			double rate_for_damp = rate_diff_pair_for_damp.getKey();
			double error_for_damp = rate_diff_pair_for_damp.getValue();
			Map<Integer, Double> forecasts_damp = damp.forecast(useful_rtms_ave_list, num_examined_mutants, num_total_mutants, grad, rate_for_damp);
			double ams_damp = Forecast.getFinal(forecasts_damp);
			boolean valid_ams_damp = Forecast.isValid(ams_damp, num_examined_mutants, num_killed_mutants, num_total_mutants);
			// Parabola
			Parabola parabola = new Parabola();
			Pair<Double, Double> rate_diff_pair_for_parabola = parabola.getAccelerationRate(useful_rtms_ave_list, num_total_mutants, grad * -1);
			double rate_for_parabola = rate_diff_pair_for_parabola.getKey();
			double error_for_parabola = rate_diff_pair_for_parabola.getValue();
			Map<Integer, Double> forecasts_parabola = parabola.forecast(useful_rtms_ave_list, num_examined_mutants, num_total_mutants, grad, rate_for_parabola);
			double ams_parabola = Forecast.getFinal(forecasts_parabola);
			boolean valid_ams_parabola = Forecast.isValid(ams_parabola, num_examined_mutants, num_killed_mutants, num_total_mutants);
			
			// If useful data has small deviation AND fitting error is small
			// Damp might be superior to Parabola
			// because the farmer might answer AMS being closer to current RtMS 
			if (useful_rtms_stats.getStandardDeviation() < 0.05 && error_for_parabola < 0.01 && error_for_damp < 0.01) {
				if (valid_ams_damp && error_for_damp <= error_for_parabola) {
					ams = ams_damp;
				}
				else if (valid_ams_parabola && error_for_parabola <= error_for_damp) {
					ams = ams_parabola;
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
			}
		}
		if (ams != null) {
			AMS.getInstance().insert(num_examined_mutants, ams);
            logger.info("Approximate mutation score: {} @ {}", ams, num_examined_mutants);
//			Executor.decideQuit();
		}
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

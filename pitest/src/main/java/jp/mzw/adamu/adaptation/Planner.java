package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.adaptation.knowledge.SAMS;
import jp.mzw.adamu.adaptation.model.forecast.Damp;
import jp.mzw.adamu.adaptation.model.forecast.Forecast;
import jp.mzw.adamu.scale.Scale;

/**
 * Plan function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Planner {
    static Logger logger = LoggerFactory.getLogger(Planner.class);
    
    public static void forecastAms(List<RtMS> usefulRtmsList, RtMS curRtms, int N) throws SQLException {
    	long start = System.currentTimeMillis();

		Double ams = null;
		// Manipulating time series data
		List<Double> useful_rtms_list = new ArrayList<>();
		List<Double> useful_rtms_ave_list = new ArrayList<>();
		double score_sum = 0;
		for (int i = 0; i < usefulRtmsList.size(); i++) {
			double score = usefulRtmsList.get(i).getScore();
			useful_rtms_list.add(score);
			score_sum += score;
			double score_ave = score_sum / (i + 1);
			useful_rtms_ave_list.add(score_ave);
		}
		int num_examined_mutants = curRtms.getNumExaminedMutants();
		int num_killed_mutants = curRtms.getNumKilledMutants();
		int num_total_mutants = N;
		// Forecasting approximate mutation score
		double grad = Forecast.getInitialGrad(useful_rtms_ave_list, num_total_mutants);
		double rate = new Damp().fit(useful_rtms_list, num_total_mutants, grad);
		Map<Integer, Double> forecasts = new Damp().forecast(useful_rtms_list, num_examined_mutants, num_total_mutants, grad, rate);
		double forecast = Forecast.getFinal(forecasts);
		if (Forecast.isValid(forecast, num_examined_mutants, num_killed_mutants, num_total_mutants)) {
			ams = forecast;
		}
		
		long end = System.currentTimeMillis();
		Overhead.getInstance().insert(Overhead.Type.Forecast, end - start);
		
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

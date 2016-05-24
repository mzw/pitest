package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.SAMS;
import jp.mzw.adamu.scale.Scale;

/**
 * Plan function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Planner {
    static Logger logger = LoggerFactory.getLogger(Planner.class);
    
    //--------------------------------------------------
    public static void quitSuggestion(int numExaminedMutants, double score, Scale scale) throws InstantiationException, IllegalAccessException, SQLException {
        AMS amsArrayPair = AMS.getInstance().getAmsArray();
        int[] timeArray = amsArrayPair.getTimeArray();
        double[] amsArray = amsArrayPair.getScoreArray();
        
        long start = System.currentTimeMillis();
        boolean shouldSuggest = Planner.shouldSuggestApproximateMutationScore(timeArray, amsArray, scale);
        long end = System.currentTimeMillis();
        Overhead.getInstance().insert(Overhead.Type.Converge, end - start);
        
        System.gc();
        
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

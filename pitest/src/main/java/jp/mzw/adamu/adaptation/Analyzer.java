package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.Random;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.scale.Scale;

import org.espy.arima.ArimaFitter;
import org.espy.arima.ArimaForecaster;
import org.espy.arima.ArimaProcess;
import org.espy.arima.DefaultArimaForecaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xal.extension.fit.DampedSinusoidFit;

/**
 * Analyze function of MAPE-K control loop implemented in AdaMu
 * @author Yuta Maezawa
 */
public class Analyzer {
    static Logger logger = LoggerFactory.getLogger(Analyzer.class);

    public static final int TRAIN_DATA_NUM = 200; // 200-300 based on heuristic
    
    public static void analyzeApproximateMutationScore(RtMS rtms) throws SQLException, InstantiationException, IllegalAccessException {
        int numTotalMutants = Stats.getInstance().getNumTotalMutants();
        int numExaminedMutants = rtms.getNumExaminedMutants();
        int numTests = Stats.getInstance().getNumTests();
        Scale scale = Scale.getScale(numTotalMutants, numTests);
        
        if(Analyzer.skipAnalyze(numExaminedMutants, scale)) {
		    return;
		}

        int noise = numTotalMutants * scale.getNoiseFilter() / 100;
        if (noise + Analyzer.TRAIN_DATA_NUM < numExaminedMutants && numExaminedMutants < numTotalMutants) {
            double[] rtmsArray = RtMS.getInstance().getRtmsArray();
            
            long start = System.currentTimeMillis();
            double ams = Analyzer.forecastAmsWithEds(rtmsArray, numTotalMutants, noise, rtms);
            long end = System.currentTimeMillis();
            Overhead.getInstance().insert(Overhead.Type.Forecast, end - start);
            
            System.gc();

            logger.info("Approximate mutation score: {}", ams);
            if (!Analyzer.isValid(ams, rtms, numTotalMutants)) {
                logger.info("Validation result: invalid");
                return;
            }
            logger.info("Validation result: valid");
            
            AMS.getInstance().insert(numExaminedMutants, ams);
            Planner.quitSuggestion(numExaminedMutants, ams, scale);
        }
    }

    private static boolean skipAnalyze(int numExaminedMutants, Scale scale) {
        if (numExaminedMutants % scale.getAnalyzeInterval() != 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Check whether approximate mutation score is valid or not.
     * A valid score should range from/to mutation score when all remaining mutants will be killed or survived
     * @param ams
     * @param rtms
     * @param num_exercised_mutants
     * @param numTotalMutants
     * @return
     */
    private static boolean isValid(double ams, RtMS rtms, int numTotalMutants) {
        int numKilledMutants = rtms.getNumKilledmutants();
        int numRemainingMutants = numTotalMutants - rtms.getNumExaminedMutants();
        
        int numMaxKilledMutants = numKilledMutants + numRemainingMutants;

        double maxMutationScore = (double) numMaxKilledMutants / (double) numTotalMutants;
        double minMutationScore = (double) numKilledMutants / (double) numTotalMutants;
        
        if (minMutationScore <= ams && ams <= maxMutationScore) {
            return true;
        }
        return false;
    }

    /**
     * Forecast approximate mutation score with exponentially damped sinusoids
     * @param rtmsArray Time series data of runtime mutation scores
     * @param numTotalMutants The total number of generated mutants
     * @param noise The number of earlier mutants as noise
     * @param rtms Current runtime mutation score
     * @return Approximate mutation score
     */
    private static double forecastAmsWithEds(double[] rtmsArray, int numTotalMutants, int noise, RtMS rtms) {
    	try {
	        DampedSinusoidFit dsf = DampedSinusoidFit.getInstance(rtmsArray);
	        dsf.solveWithNoiseMaxEvaluationsSatisfaction(0, 1, 0);
	        double amp = dsf.getAmplitude();
	        double exp = Math.exp(dsf.getGrowthRate() * numTotalMutants);
	        double sin = Math.sin(2 * Math.PI * dsf.getFrequency() * numTotalMutants + dsf.getPhase());
	        double offset = dsf.getOffset();
	        return amp * exp * sin + offset;
    	} catch (RuntimeException e) {
    		return rtms.getScore();
    	}
    }

    @SuppressWarnings("unused")
    private static double forecastWithARIMA(double[] rtmsArray, int numTotalMutants, Scale scale, int noise) {
        int numExaminedMutants = rtmsArray.length;
        
        double[] samples = null;
        if (numExaminedMutants - noise < TRAIN_DATA_NUM) {
            samples = new double[rtmsArray.length - noise];
            for (int i = noise + 1; i < rtmsArray.length; i++) {
                samples[i - noise - 1] = rtmsArray[i];
            }
        } else {
            samples = new double[TRAIN_DATA_NUM];
            double sep = ((double) (numExaminedMutants - noise)) / (double) TRAIN_DATA_NUM;
            for (int i = 0; i < TRAIN_DATA_NUM; i++) {
                int index = ((int) (sep * (i + 1))) + noise - 1;
                samples[i] = rtmsArray[index];
            }
        }
        
        ArimaProcess process = ArimaFitter.fit(samples);
        ArimaForecaster forecaster = new DefaultArimaForecaster(process, samples);
        double[] forecast = forecaster.next(numTotalMutants - numExaminedMutants);
        
        return forecast[forecast.length - 1];
    }
    
    @SuppressWarnings("unused")
    private static double forecast(RtMS rtms, int numTotalMutants) {
        int numWouldKilledMutants = 0;
        Random randam = new Random();
        for (int i = 0; i < numTotalMutants - rtms.getNumExaminedMutants(); i++) {
            if (randam.nextDouble() < rtms.getScore()) {
                numWouldKilledMutants++;
            }
        }
        return (double) (rtms.getNumKilledmutants() + numWouldKilledMutants) / (double) numTotalMutants;
    }
}

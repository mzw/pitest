package jp.mzw.adamu.adaptation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.mzw.adamu.adaptation.knowledge.AMS;
import jp.mzw.adamu.adaptation.knowledge.Overhead;
import jp.mzw.adamu.adaptation.knowledge.Stats;
import jp.mzw.adamu.adaptation.knowledge.RtMS;
import jp.mzw.adamu.adaptation.model.ConvergeDiagnostic;
import jp.mzw.adamu.adaptation.model.SPRT;
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

    public static void analyze(List<RtMS> rtmsList, RtMS curRtms) throws SQLException {
    	int num_total_mutants = Stats.getInstance().getNumTotalMutants();
    	
    	boolean burnin = true;
    	boolean stop = false;
    	List<RtMS> cur_rtms_list = new ArrayList<>();
    	List<RtMS> cur_useful_rtms_list = new ArrayList<>();
    	for (RtMS rtms : rtmsList) {
    		cur_rtms_list.add(rtms);
    		if (burnin) {
    			long start = System.currentTimeMillis();
    			boolean converge = ConvergeDiagnostic.converge(cur_rtms_list, num_total_mutants);
    			long end = System.currentTimeMillis();
                Overhead.getInstance().insert(Overhead.Type.Converge, end - start);
    			if (converge) {
    				burnin = false;
    			} else {
    				// Burn-in period
    			}
    		} else {
    			cur_useful_rtms_list.add(rtms);
    			if (stop) {
    				// Forecasting approximate mutation score
    			} else {
    				long start = System.currentTimeMillis();
    				boolean stop_decision = SPRT.stop(curRtms.getNumExaminedMutants(), curRtms.getNumKilledMutants(), num_total_mutants);
    				long end = System.currentTimeMillis();
                    Overhead.getInstance().insert(Overhead.Type.StopDecision, end - start);
        			if (stop_decision) {
        				stop = true;
        			} else {
        				// Continue to measure for making stop decision
        			}
    			}
    		}
    	}
    	
    	if (burnin) {
    		logger.info("Burn-in period...");
    	} else if (!stop) {
    		logger.info("Continue to measure for making stop decision...");
    	} else {
            logger.info("Suggest for stopping mutation testing");
    		Planner.forecastAms(cur_useful_rtms_list, curRtms, num_total_mutants);
    	}
    }

    //--------------------------------------------------
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
        int numKilledMutants = rtms.getNumKilledMutants();
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
    		double[] samples = sample(rtmsArray, noise);
	        DampedSinusoidFit dsf = DampedSinusoidFit.getInstance(samples);
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

    /**
     * Forecast approximate mutation score with autoregressive, integrated, moving average model
     * @param rtmsArray Time series data of runtime mutation scores
     * @param numTotalMutants The total number of generated mutants
     * @param noise The number of earlier mutants as noise
     * @return
     */
    @SuppressWarnings("unused")
    private static double forecastWithARIMA(double[] rtmsArray, int numTotalMutants, int noise) {
        double[] samples = sample(rtmsArray, noise);
        ArimaProcess process = ArimaFitter.fit(samples);
        ArimaForecaster forecaster = new DefaultArimaForecaster(process, samples);
        double[] forecast = forecaster.next(numTotalMutants - rtmsArray.length);
        return forecast[forecast.length - 1];
    }
    
    /**
     * Sample time series data without noise
     * @param data Given time series data
     * @param noise Given noise
     * @return Sampled time series data
     */
    private static double[] sample(double[] data, int noise) {
        double[] samples = null;
        if (data.length - noise < TRAIN_DATA_NUM) {
            samples = new double[data.length - noise];
            for (int i = noise + 1; i < data.length; i++) {
                samples[i - noise - 1] = data[i];
            }
        } else {
            samples = new double[TRAIN_DATA_NUM];
            double sep = ((double) (data.length - noise)) / (double) TRAIN_DATA_NUM;
            for (int i = 0; i < TRAIN_DATA_NUM; i++) {
                int index = ((int) (sep * (i + 1))) + noise - 1;
                samples[i] = data[index];
            }
        }
        return samples;
    }
}

package jp.mzw.adamu.adaptation.model;

import java.util.List;

import org.pitest.mutationtest.DetectionStatus;

public class SPRT {

	/** To be decided */
	public static enum Action {
		AcceptH1, AcceptH2, Continue,
	}

	/** Significance level */
	public static final double ALPHA = 0.05;

	/** Critical region (power = 1 - BETA) */
	public static final double BETA = 0.1;
	
	
	public static boolean stop(int numEaminedMutants, int numKilledMutants, int numTotalMutants) {
		if (!decide(numEaminedMutants, numKilledMutants, numTotalMutants).equals(Action.Continue)) {
			return true;
		}
		return false;
	}
	
	private static Action decide(int n, int k, int N) {
		double theta1 = 0.5 - (5 / (double) N);
		double theta2 = 1 - theta1;
		double lower = getLowerBound(n, ALPHA, BETA, theta1,
				theta2);
		double upper = getUpperBound(n, ALPHA, BETA, theta1,
				theta2);

		if (k < lower)
			return Action.AcceptH2;
		else if (upper < k)
			return Action.AcceptH1;
		else
			return Action.Continue;
		
	}
	
	public static boolean stop(List<DetectionStatus> observations, int total) {
//		if (observations.size() < 85) {
//			return false;
//		}
		if (!decide(observations, total).equals(Action.Continue)) {
			return true;
		}
		return false;
	}

	/**
	 * Sequentially probability ratio test
	 * 
	 * @param observations
	 * @return
	 */
	public static Action decide(List<DetectionStatus> observations, int N) {
		int k = 0;
		for (DetectionStatus observation : observations) {
			if (observation.equals(DetectionStatus.KILLED)
					|| observation.equals(DetectionStatus.MEMORY_ERROR)
					|| observation.equals(DetectionStatus.TIMED_OUT)
					|| observation.equals(DetectionStatus.RUN_ERROR)) {
				k++;
			}
		}

//		double diff = (1 / (double) N) > 0.001 ? (1 / (double) N) : 0.001;
//		double theta1 = 0.5 - (5 / (double) N);
		double theta1 = 0.5 - Math.pow(N, -1) * 5;
//		double theta1 = 0.5 - diff;
		double theta2 = 1 - theta1;
		//    theta2    /    theta1    = (N + 10) / (N - 10)
		// (1 - theta2) / (1 - theta1) = (N - 10) / (N + 10)

		double lower = getLowerBound(observations.size(), ALPHA, BETA, theta1,
				theta2);
		double upper = getUpperBound(observations.size(), ALPHA, BETA, theta1,
				theta2);

		if (k < lower)
			return Action.AcceptH2;
		else if (upper < k)
			return Action.AcceptH1;
		else
			return Action.Continue;

	}

	private static double getLowerBound(int k, double alpha, double beta,
			double theta1, double theta2) {
		double a = Math.log(beta / (1 - alpha));
		double b = Math.log((1 - theta2) / (1 - theta1));
		double c = Math.log(theta2 / theta1);
		double d = Math.log((1 - theta2) / (1 - theta1));
		return (a - k * b) / (c - d);
	}

	private static double getUpperBound(int k, double alpha, double beta,
			double theta1, double theta2) {
		double a = Math.log((1 - beta) / alpha);
		double b = Math.log((1 - theta2) / (1 - theta1));
		double c = Math.log(theta2 / theta1);
		double d = Math.log((1 - theta2) / (1 - theta1));
		return (a - k * b) / (c - d);
	}

}

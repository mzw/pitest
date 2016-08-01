package jp.mzw.adamu.adaptation.knowledge;

import org.pitest.mutationtest.DetectionStatus;

public abstract class KnowledgeBase {

	public static final String TAB = "\t";
	public static final String COMMA = ",";
	public static final String BR = "\n";

	public abstract void output();

	public static boolean isKilled(DetectionStatus observation) {
		if (observation.equals(DetectionStatus.KILLED) || observation.equals(DetectionStatus.MEMORY_ERROR) || observation.equals(DetectionStatus.TIMED_OUT)
				|| observation.equals(DetectionStatus.RUN_ERROR)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static double getScore(int numExaminedMutants, int numKilledMutants) {
		return (double) numKilledMutants / (double) numExaminedMutants;
	}

}

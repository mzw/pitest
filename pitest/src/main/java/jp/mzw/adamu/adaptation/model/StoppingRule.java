package jp.mzw.adamu.adaptation.model;

import java.util.ArrayList;
import java.util.List;

import org.pitest.mutationtest.DetectionStatus;

import jp.mzw.adamu.adaptation.knowledge.KnowledgeBase;
import jp.mzw.adamu.adaptation.model.sprt.SPRT;

public class StoppingRule extends ModelBase {
	
	public static boolean stop(List<DetectionStatus> observations, int n, int N) {

		// When sampling more than 75% of created mutants,
		// AdaMu makes a decision as RtMS time series become converged by design
		if (N * 0.75 < n) {
			return true;
		}
		
		// Measure fraction of live mutants
		List<Double> values = new ArrayList<>();
		// Sampling
		int obs_smaple_sep = (int) Math.floor(N * 0.001);
		if (obs_smaple_sep == 0) {
			obs_smaple_sep = 1;
		}
		for (int i = 0; i < observations.size(); i++) {
			if (i % obs_smaple_sep == 0) {
				DetectionStatus status = observations.get(i);
				if (!KnowledgeBase.isKilled(status)) {
					values.add(new Double(1));
				} else {
					values.add(new Double(0));
				}
			}
		}
		
		double progress = (double) n / (double) N;
		double a1 = 0.5 - (0.01 * progress);
		double a2 = 1 - a1;
		
		// Make decision
		SPRT.Action action = SPRT.fn(toDoubleArray(values), a1, a2);
		if (action.equals(SPRT.Action.ContinueTesting)) {
			return false;
		} else {
			return true;
		}
	}

//	public static boolean stop(final List<DetectionStatus> observations, final int n, final int k, final int N) {
//
//		if (observations.size() < 101) {
//			return false;
//		}
//		
////		if (10000 < N) {
////			if (N * 0.5 < n) {
////				return true;
////			}
////		} else if (1000 < N) {
////			if (N * 0.6 < n) {
////				return true;
////			}
////		} else {
////			if (N * 0.7 < n) {
////				return true;
////			}
////		}
//
//		double score = (double) k / (double) n;
//		double progress = (double) n / (double) N;
//		
//		// Measure fraction of live mutants
//		List<Double> values = new ArrayList<>();
//		// Sampling
//		int obs_smaple_sep = (int) Math.floor(N * 0.001);
//		if (obs_smaple_sep == 0) {
//			obs_smaple_sep = 1;
//		}
//		for (int i = 0; i < observations.size(); i++) {
//			if (i % obs_smaple_sep == 0) {
//				DetectionStatus status = observations.get(i);
//				if (!isKilled(status)) {
//					values.add(new Double(1));
//				} else {
//					values.add(new Double(0));
//				}
//			}
//		}
//		
//		double delta = 0.01;
//		double diff = Math.abs(0.5 - score);
//		if (diff < 0.1) {
//			delta *= 4;
//		} else if (diff < 0.2) {
//			delta *= 1;//2
//		} else if (diff < 0.3) {
//			delta *= 1;
//		} else if (diff < 0.4) {
//			delta *= 0.5;
//		} else {
//			delta *= 0.1;
//		}
//		delta *= progress;
//		
//		double a1 = 0.5 - delta;
//
//		double a2 = 1 - a1;
//		// Make decision
//		SPRT.Action action = SPRT.fn(toDoubleArray(values), a1, a2);
//		if (action.equals(SPRT.Action.ContinueTesting)) {
//			return false;
//		} else {
//			return true;
//		}
//	}
}

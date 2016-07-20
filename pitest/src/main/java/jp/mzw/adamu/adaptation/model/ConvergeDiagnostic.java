package jp.mzw.adamu.adaptation.model;

import java.util.HashMap;
import java.util.List;

import jp.mzw.adamu.adaptation.knowledge.RtMS;
import bb.mcmc.analysis.RafteryConvergeStat;
import dr.math.distributions.NormalDistribution;

public class ConvergeDiagnostic {
	public static boolean converge(double[] data, int N) {
		double _e = 0.005;
		double _p = 0.95;
		// Solve quartile
		double min_mutation_rate = 0.05;
		if (10000 < N) {
			min_mutation_rate = 0.01;
		} else if (N < 1000) {
			min_mutation_rate = 0.1;
		}
		double nd = NormalDistribution.quantile(0.5 * (1 + _p), 0, 1);
		double a = 1;
		double b = -1;
		double c = (N * min_mutation_rate) * Math.pow(_e / nd, 2);
		double _q = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		if (Double.isNaN(_q) | _q == 0) {
			return false;
		}
		// Diagnostic
		RafteryConvergeStat raftery = new RafteryConvergeStat(_q, _e, _p, 0.001, 5);
		if (data.length < raftery.getNMin()) {
			return false;
		}
		final String var_name = "adamu";
		raftery.setTestVariableName(new String[]{var_name});
		HashMap<String, double[]> sample_values = new HashMap<>();
		sample_values.put(var_name, data);
		raftery.updateValues(sample_values);
		raftery.calculateStatistic();
		return raftery.haveAllConverged();
	}

	public static boolean converge(List<RtMS> rtmsList, int N) {
		double[] data = new double[rtmsList.size()];
		for (int i = 0; i < rtmsList.size(); i++) {
			RtMS rtms = rtmsList.get(i);
			data[i] = rtms.getScore();
		}
		return converge(data, N);
	}
}

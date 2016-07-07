package jp.mzw.adamu.adaptation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import jp.mzw.adamu.adaptation.knowledge.RtMS;
import bb.mcmc.analysis.RafteryConvergeStat;
import dr.math.distributions.NormalDistribution;

public class ConvergeDiagnostic {
	
	public static List<RtMS> getUsefulRtmsList(List<RtMS> rtmsList, int N) {
		List<RtMS> useful_rtms_list = new ArrayList<>();
		
		ConvergeDiagnostic cd = new ConvergeDiagnostic();
		List<Double> cur_rtms_list = new ArrayList<>();
		boolean converged = false;
		
		for (RtMS rtms : rtmsList) {
			cur_rtms_list.add(rtms.getScore());
			if (!converged) {
				converged = cd.converge(toDoubleArray(cur_rtms_list), N);
			}
			if (converged) {
				useful_rtms_list.add(rtms);
			}
		}
		
		return useful_rtms_list;
	}

	public static double[] toDoubleArray(List<Double> list) {
		Double[] array = list.toArray(new Double[0]);
		return ArrayUtils.toPrimitive(array);
	}

	protected double e_adjust;
	protected double p_adjust;
	protected int num_adjust;
	
	protected double min_mutation_rate;

	public ConvergeDiagnostic() {
		e_adjust = 0;
		e_adjust = 0;
		num_adjust = 0;
		
		min_mutation_rate = 0.01;
	}
	
	/** Estimation accuracy */
	public static final double E = 0.005;
	
	/** Confidence interval */
	public static final double P = 0.95;
	
	/** Minimum number of mutants for converge diagnostic */
	public static final double MIN_NUM_MUTANTS = 100;

	public boolean converge(double[] data, int N) {
		double _e = E + e_adjust;
		double _p = P + p_adjust;
		
		if (N * min_mutation_rate < MIN_NUM_MUTANTS) {
			min_mutation_rate = 0.1;
		}
		
		double nd = NormalDistribution.quantile(0.5 * (1 + _p), 0, 1);
		
		double a = 1;
		double b = -1;
		double c = (N * min_mutation_rate) * Math.pow(_e / nd, 2);
		
		double q = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		
		if (Double.isNaN(q)) {
			++num_adjust;
			e_adjust -= 0.001 / Math.pow(2, num_adjust);
			p_adjust -= 0.05 / Math.pow(2, num_adjust);
			return false;
		}
		
		RafteryConvergeStat raftery = new RafteryConvergeStat(q, _e, _p, 0.001, 5);
		int n_min = raftery.getNMin();
		
		if (data.length < raftery.getNMin()) {
			return false;
		}
		
		double[] samples = new double[raftery.getNMin()];
		for (int i = 1; i <= raftery.getNMin(); i++) {
			samples[i - 1] = data[data.length - i];
		}
		final String var_name = "adamu";
		raftery.setTestVariableName(new String[]{var_name});
		HashMap<String, double[]> sample_values = new HashMap<>();
		sample_values.put(var_name, data);
		raftery.updateValues(sample_values);
		raftery.calculateStatistic();
		boolean converged = raftery.haveAllConverged();
		
		if (converged && Math.abs(data.length - n_min) < N * 0.01) {
			++num_adjust;
			e_adjust += 0.001 / Math.pow(2, num_adjust);
			p_adjust += 0.05 / Math.pow(2, num_adjust);
			return false;
		}		
		return converged;
	}

}

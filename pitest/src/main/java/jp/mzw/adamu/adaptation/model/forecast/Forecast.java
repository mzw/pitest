package jp.mzw.adamu.adaptation.model.forecast;

import java.util.List;
import java.util.Map;

public abstract class Forecast implements IForecast {

	public static double getInitialGrad(List<Double> data, int N) {
		int num_prv = (int) (N * 0.01);
		double prv = data.get(data.size() - num_prv - 1);
		double cur = data.get(data.size() - 1);
		double grad = cur - prv;
		return grad;
	}

	public static double getInitialGrad(List<Double> data, int N, int n) {
		int _n = data.size();
		double _xi_sum = 0;
		double _yi_sum = 0;
		double _xi_xi_sum = 0;
		double _xi_yi_sum = 0;
		for (int i = 1; i <= _n; i++) {
			double _xi = i;
			double _yi = data.get(i - 1);
			_xi_sum += _xi;
			_yi_sum += _yi;
			_xi_xi_sum += _xi * _xi;
			_xi_yi_sum += _xi * _yi;
			
		}
		double grad = (_n * _xi_yi_sum - _xi_sum * _yi_sum) / (_n * _xi_xi_sum - _xi_sum * _xi_sum);
		return grad * Math.floor(N * 0.01);
	}

	public static Double getFinal(List<Double> data) {
		int size = data.size();
		if (data.isEmpty()) {
			return null;
		}
		return data.get(size - 1);
	}

	public static double getFinal(Map<Integer, Double> data) {
		if (data == null) {
			return -1;
		}
		if (data.size() == 0) {
			return -1;
		}
		double ret = -1;
		Integer max_index = Integer.MIN_VALUE;
		for (Integer index : data.keySet()) {
			if (max_index < index) {
				ret = data.get(index);
			}
		}
		return ret;
	}

    public static boolean isValid(double ams, int numExaminedMutants, int numKilledMutants, int numTotalMutants) {
        int numRemainingMutants = numTotalMutants - numExaminedMutants;
        int numMaxKilledMutants = numKilledMutants + numRemainingMutants;
        double maxMutationScore = (double) numMaxKilledMutants / (double) numTotalMutants;
        double minMutationScore = (double) numKilledMutants / (double) numTotalMutants;
        if (minMutationScore <= ams && ams <= maxMutationScore) {
            return true;
        }
        return false;
    }

	public abstract Map<Integer, Double> forecast(List<Double> data, int n, int N, double grad, double rate);
	
}

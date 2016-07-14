package jp.mzw.adamu.adaptation.model.forecast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Damp extends Forecast {
	
	public Damp() {
		super();
	}
	
	@Override
	public double fit(List<Double> data, int N, double grad) {
		double fit_rate = Double.MAX_VALUE;
		double min_squared_rasiduals_sum = Double.MAX_VALUE;
		for (double rate = 0; rate <= 1; rate += Math.pow(N, -1)) {
			double srs = getSquaredResidualsSum(data, N, grad, rate);
			if (srs < min_squared_rasiduals_sum) {
				min_squared_rasiduals_sum = srs;
				fit_rate = rate;
			}
		}
		return fit_rate;
	}
	
	private static double getSquaredResidualsSum(List<Double> data, int N, double grad, double rate) {
		int _step = (int) Math.floor(N * 0.01);
		double _ams = data.get(data.size() - 1);
		double _grad = grad * -1;
		double _rate = rate;

		double squared_residuals_sum = 0;
		for (int i = 0; i < data.size(); i += _step) {
			double _data = data.get(data.size() - 1 - i);
			squared_residuals_sum += Math.pow(_ams - _data, 2);
			_ams += _grad;
			_grad /= (1 - _rate);
		}
		return squared_residuals_sum;
	}
	
	@Override
	public Map<Integer, Double> forecast(List<Double> data, int n, int N, double grad, double rate) {
		if (n < 1 || N < n) {
			return null;
		}
		if (data.size() < (int) (N * 0.01) + 1) {
			return null;
		}
		int num_prv = (int) (N * 0.01);
		double cur_rtms_ave = getFinal(data);
		double ams = cur_rtms_ave;
		Map<Integer, Double> ret = new HashMap<>();
		while (n < N) {
			ams += grad;
			n += num_prv;
			grad *= (1 - rate);
			ret.put(n, ams);
		}
		return ret;
	}
	
}

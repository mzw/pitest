package jp.mzw.adamu.adaptation.model.forecast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

public class Parabola extends Forecast {

	@Override
	public Pair<Double, Double> getAccelerationRate(List<Double> data, int N, double grad) {
		double min_diff = Double.MAX_VALUE;
		double min_rate = -1;
		for (double rate = 0.05; rate < 1; rate += Math.pow(N, -1)) {
			double diff = getAccelerationRate(data, N, grad, rate);
			if (diff < min_diff) {
				min_diff = diff;
				min_rate = rate;
			}
		}
		return Pair.create(min_rate, min_diff);
	}

	private double getAccelerationRate(List<Double> data, int N, double grad, double rate) {
		int _size = data.size();
		int _step = (int) Math.ceil(N * 0.01);

		double _cur_ams = data.get(_size - 1);
		double _grad = grad;

		double _ams = _cur_ams;
		boolean _rebound = true;
		double diff_sum = 0;
		int _step_num = 0;
		for (int _i = 1; _i <= _size; _i += _step) {
			_step_num++;
			if (_size == _i)
				--_i;
			double _data = data.get(_size - _i - 1);
			diff_sum += Math.abs(_data - _ams);
			_ams += _grad;
			_grad *= (1 - rate);
			if (_rebound && Math.abs(_grad) < 0.001) {
				_grad *= -1;
				rate *= -1;
				_rebound = false;
			}
		}
		return diff_sum / (double) _step_num;
	}
	
	@Override
	public double forecastFinal(List<Double> data, int n, int N, double grad, double rate) {
		if (n < 1 || N < n) {
			return -1;
		}
		if (data.size() < (int) (N * 0.01) + 1) {
			return -1;
		}

		int num_prv = (int) (N * 0.01);
		double ams = getFinal(data);
		boolean _rebound = true;
		while (n < N) {
			ams += grad;
			n += num_prv;
			grad *= (1 - rate);
			if (_rebound && Math.abs(grad) < 0.001) {
				grad *= -1;
//				rate *= -1;
				rate *= -0.5;
				_rebound = false;
			}
		}
		return ams;
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
		double ams = getFinal(data);
		boolean _rebound = true;
		
		Map<Integer, Double> ret = new HashMap<>();
		while (n < N) {
			ams += grad;
			n += num_prv;
			grad *= (1 - rate);
			
			ret.put(n, ams);
			
			if (_rebound && Math.abs(grad) < 0.001) {
				grad *= -1;
//				rate *= -1;
				rate *= -0.5;
				_rebound = false;
			}
		}
		return ret;
	}

	
}

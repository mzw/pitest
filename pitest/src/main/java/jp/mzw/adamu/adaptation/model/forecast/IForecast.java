package jp.mzw.adamu.adaptation.model.forecast;

import java.util.List;
import java.util.Map;

public interface IForecast {

	public double fit(List<Double> data, int N, double grad);
	public Map<Integer, Double> forecast(List<Double> data, int n, int N, double grad, double rate);
	
}

package jp.mzw.adamu.adaptation.model.forecast;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

public interface IForecast {


	public Pair<Double, Double> getAccelerationRate(List<Double> data, int N, double grad);
	
	public double forecastFinal(List<Double> data, int n, int N, double grad, double rate);
	public Map<Integer, Double> forecast(List<Double> data, int n, int N, double grad, double rate);
	
}

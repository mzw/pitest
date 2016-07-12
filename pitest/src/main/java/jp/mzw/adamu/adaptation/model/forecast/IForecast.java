package jp.mzw.adamu.adaptation.model.forecast;

import java.util.List;
import java.util.Map;

public interface IForecast {

	public void calcAccelerationRate(List<Double> data, int N, double grad);
	public double getAccelerationRate();
	public double getAccelerationRateError();
	
	public double forecastFinal(List<Double> data, int n, int N, double grad, double rate);
	public Map<Integer, Double> forecast(List<Double> data, int n, int N, double grad, double rate);
	
}

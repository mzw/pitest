package jp.mzw.adamu.adaptation.model.sprt;

public class D {
	public static Double fn(Distribution.Type dist, double h) {
		if (dist.equals(Distribution.Type.Normal)) {
			return Math.pow(h, 2) / 2;
		} else if (dist.equals(Distribution.Type.Bernoulli)) {
			return -1.0 * Math.log(1 - h);
		} else if (dist.equals(Distribution.Type.Poisson)) {
			return h;
		} else if (dist.equals(Distribution.Type.Exponential)) {
			return Math.log(h);
		}
		return null;
	}
}

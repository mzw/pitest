package jp.mzw.adamu.adaptation.model.sprt;

public class C {
	public static Double fn(Distribution.Type dist, double h) {
		if (dist.equals(Distribution.Type.Normal)) {
			return h;
		} else if (dist.equals(Distribution.Type.Bernoulli)) {
			return Math.log(h / (1 - h));
		} else if (dist.equals(Distribution.Type.Poisson)) {
			return Math.log(h);
		} else if (dist.equals(Distribution.Type.Exponential)) {
			return -1.0 / h;
		}
		return null;
	}
}

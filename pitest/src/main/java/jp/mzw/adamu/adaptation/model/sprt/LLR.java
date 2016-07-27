package jp.mzw.adamu.adaptation.model.sprt;

public class LLR {
	public static KN fn(Distribution.Type dist, double h0, double h1) {
		double k = C.fn(dist, h1) - C.fn(dist, h0);
		double n = (D.fn(dist, h1) - D.fn(dist, h0)) * -1.0;
		return new KN(k, n);
	}
	
	public static class KN {
		private double k;
		private double n;
		private KN(double k, double n) {
			this.k = k;
			this.n = n;
		}
		public double getK() {
			return this.k;
		}
		public double getN() {
			return this.n;
		}
	}

	public static double closure(KN coefficients, double n, double k) {
		return n * coefficients.getN() + k * coefficients.getK();
	}
}

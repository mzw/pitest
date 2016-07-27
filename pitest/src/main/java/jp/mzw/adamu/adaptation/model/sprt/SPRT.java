package jp.mzw.adamu.adaptation.model.sprt;

public class SPRT {
	
	public static final Distribution.Type DEFAULT_DIST = Distribution.Type.Bernoulli;
	public static final double DEFAULT_TYPE1 = 0.05;
	public static final double DEFAULT_TYPE2 = 0.20;
	
	public static Action fn(final double[] values, double h0, double h1) {
		// Number of observations
		double n = values.length;
		// Sum the random variable
		double k = 0;
		for (double v : values) {
			k += v;
		}
		// Wald boundaries
		double boundary_a = WaldBoundary.getA(DEFAULT_TYPE1, DEFAULT_TYPE2);
		double boundary_b = WaldBoundary.getB(DEFAULT_TYPE1, DEFAULT_TYPE2);
		// Decision
		double llr = LLR.closure(LLR.fn(DEFAULT_DIST, h0, h1), n, k);
		if (llr >= boundary_a) {
			return Action.AcceptH1;
		} else if (llr <= boundary_b) {
			return Action.AcceptH0;
		} else {
			return Action.ContinueTesting;
		}
	}

	public static enum Action {
		AcceptH1,
		AcceptH0,
		ContinueTesting,
	}
	
}

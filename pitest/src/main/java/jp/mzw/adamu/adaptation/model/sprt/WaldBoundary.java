package jp.mzw.adamu.adaptation.model.sprt;

public class WaldBoundary {
	public static double getA(double type1, double type2, boolean log) {
		double a = (1 - type2) / type1;
		if (log) {
			return Math.log(a);
		} else {
			return a;
		}
	}
	public static double getA(double type1, double type2) {
		return getA(type1, type2, true);
	}

	public static double getB(double type1, double type2, boolean log) {
		double b = type2 / (1 - type1);
		if (log) {
			return Math.log(b);
		} else {
			return b;
		}
	}
	public static double getB(double type1, double type2) {
		return getB(type1, type2, true);
	}
}

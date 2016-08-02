package jp.mzw.adamu.adaptation.model;

import java.util.List;

public class ModelBase {

	public static double[] toDoubleArray(List<Double> list) {
		if (list == null) {
			return null;
		}
		double[] ret = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	public static double[] toDoubleArray(Double[] array) {
		if (array == null) {
			return null;
		}
		double[] ret = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			ret[i] = array[i];
		}
		return ret;
	}

}

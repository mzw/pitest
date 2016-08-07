package jp.mzw.adamu.adaptation;

public class MAPE {

	/**
	 * The percentage of the number of created mutants for skipping to call the analyze function
	 */
	public static final double SKIP_INTERVAL = 0.01; // 1%
	
	/**
	 * Skip to call the analyze function for mitigating computational overhead
	 * @param n the number of examined mutants so far
	 * @param N the number of created mutants
	 * @return
	 */
	public static boolean skip(int n, int N) {
		int interval = (int) Math.ceil(N * SKIP_INTERVAL);
		if (n % interval == 0) {
			return false;
		}
		return true;
	}
}

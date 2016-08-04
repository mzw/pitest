package jp.mzw.adamu.adaptation;

import org.junit.Assert;
import org.junit.Test;

public class AnalyzerTest {

	@Test
	public void skipTestLessThan1K() {
		int N = 500;
		int interval = 1;
		for (int i = 1; i <= N; i++) {
			boolean skip = Analyzer.skip(i, N);
			if (i % interval == 0) {
				Assert.assertFalse(skip);
			} else {
				Assert.assertTrue(skip);
			}
		}
	}

	@Test
	public void skipTestLessThan10K() {
		int N = 5000;
		int interval = 5;
		for (int i = 1; i <= N; i++) {
			boolean skip = Analyzer.skip(i, N);
			if (i % interval == 0) {
				Assert.assertFalse(skip);
			} else {
				Assert.assertTrue(skip);
			}
		}
	}

	@Test
	public void skipTestMoreThan10K() {
		int N = 50000;
		int interval = 50;
		for (int i = 1; i <= N; i++) {
			boolean skip = Analyzer.skip(i, N);
			if (i % interval == 0) {
				Assert.assertFalse(skip);
			} else {
				Assert.assertTrue(skip);
			}
		}
	}
	
}

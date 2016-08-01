package jp.mzw.adamu.adaptation.knowledge.data;

import org.pitest.mutationtest.DetectionStatus;

public class TestResult extends Mutation {

	DetectionStatus status;

	public TestResult(int hashcode, String className, String methodName, int lineno, String mutator, DetectionStatus status) {
		super(hashcode, className, methodName, lineno, mutator);
		this.status = status;
	}

	public DetectionStatus getStatus() {
		return this.status;
	}

}

package jp.mzw.adamu.adaptation.knowledge.data;

public class Mutation {

	protected int hashcode;
	protected String class_name;
	protected String method_name;
	protected int lineno;
	protected String mutator;

	public Mutation(int hashcode, String className, String methodName, int lineno, String mutator) {
		this.hashcode = hashcode;
		this.class_name = className;
		this.method_name = methodName;
		this.lineno = lineno;
		this.mutator = mutator;
	}

	public int getHashcode() {
		return this.hashcode;
	}
	
	public String getClassName() {
		return this.class_name;
	}

	public String getMethodName() {
		return this.method_name;
	}

	public int getLineno() {
		return this.lineno;
	}

	public String getMutator() {
		return this.mutator;
	}
	
}

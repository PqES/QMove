package qmove.movemethod;


public class Recommendation {
	
	private String packageMethodName;
	private String classMethodName;
	private String methodName;
	private String packageTargetName;
	private String classTargetName;
	private double increase;
	
	public Recommendation(MethodsChosen method, double increase){
		packageMethodName = method.getpackageOriginal();
		classMethodName = method.getClassOriginal();
		methodName = method.getMethod().getElementName();
		packageTargetName = method.getTargetChosen().getType().getPackage().getName();
		classTargetName = method.getTargetChosen().getName();
		this.increase = increase;
	}

	public String getPackageMethodName() {
		return packageMethodName;
	}

	public String getClassMethodName() {
		return classMethodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getPackageTargetName() {
		return packageTargetName;
	}

	public String getClassTargetName() {
		return classTargetName;
	}

	public double getIncrease() {
		return increase;
	}
	
	
}

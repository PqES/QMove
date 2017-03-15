package qmove.movemethod;

import java.io.Serializable;

public class Recommendation implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int qmoveID;
	private String packageMethodName;
	private String classMethodName;
	private String methodName;
	private String packageTargetName;
	private String classTargetName;
	private double increase;
	private MethodsTable methodsTable;
	
	
	public Recommendation(int qmoveID, MethodsTable methodsTable, MethodsChosen method, double increase){
		this.qmoveID = qmoveID;
		this.methodsTable = methodsTable;
		packageMethodName = method.getpackageOriginal();
		classMethodName = method.getClassOriginal();
		methodName = method.getMethod().getElementName();
		packageTargetName = method.getTargetChosen().getType().getPackage().getName();
		classTargetName = method.getTargetChosen().getName();
		this.increase = increase;
	}
	
	public int getQMoveID(){
		return qmoveID;
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
	
	public MethodsTable getMethodsTable(){
		return methodsTable;
	}
	
	
}

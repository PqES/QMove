package qmove.movemethod;

import org.eclipse.jdt.core.IMethod;

public class ClassMethod {
	
	String packageName;
	String className;
	IMethod method;
	
	public ClassMethod(String packageName, String className, IMethod method){
		this.packageName = packageName;
		this.className = className;
		this.method = method;
		
	}

	public String getClassName() {
		return className;
	}
	
	public String getPackageName() {
		return packageName;
	}

	public IMethod getMethod() {
		return method;
	}
	
	
}

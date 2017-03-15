package qmove.movemethod;

import java.io.Serializable;

import org.eclipse.jdt.core.IMethod;


public class ClassMethod implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

package qmove.movemethod;

import org.eclipse.jdt.core.IMethod;

public class ClassMethod {
	
	String className;
	IMethod[] methods;
	
	public ClassMethod(String className, IMethod[] methods){
		this.className = className;
		this.methods = methods;
	}

	public String getClassName() {
		return className;
	}

	public IMethod[] getMethods() {
		return methods;
	}
	
	
}

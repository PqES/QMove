package qmove.movemethod;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;


public class ClassMethod {
	
	IPackageFragment packageName;
	IType className;
	IMethod method;
	
	public ClassMethod(IPackageFragment packageName, IType className, IMethod method){
		this.packageName = packageName;
		this.className = className;
		this.method = method;
		
	}

	public IType getClassName() {
		return className;
	}
	
	public IPackageFragment getPackageName() {
		return packageName;
	}

	public IMethod getMethod() {
		return method;
	}
}

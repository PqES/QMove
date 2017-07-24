package qmove.compilation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class AllMethods {
	
	private IJavaElement _package;
	private IType _class;
	private IMethod _method;
	
	public AllMethods(IJavaElement _package, IType _class, IMethod _method) {
		
		this._package = _package;
		this._class = _class;
		this._method = _method;
	}

	public IJavaElement get_package() {
		return _package;
	}

	public IType get_class() {
		return _class;
	}

	public IMethod get_method() {
		return _method;
	}
	
	

}

package qmove.movemethod;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class MethodsTargets {

	private IMethod method;
	private IVariableBinding targetChosen;
	
	public MethodsTargets(){
		
	}
	
	public IMethod getMethod() {
		return method;
	}

	public IVariableBinding getTargetChosen() {
		return targetChosen;
	}
}

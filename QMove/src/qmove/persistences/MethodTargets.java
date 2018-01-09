package qmove.persistences;

import java.util.ArrayList;

import org.eclipse.jdt.core.IMethod;

public class MethodTargets {

	IMethod method;
	ArrayList<String> targets;

	public MethodTargets(IMethod method, ArrayList<String> targets) {
		this.method = method;
		this.targets = targets;
	}

	public IMethod getMethod() {
		return method;
	}

	public ArrayList<String> getTargets() {
		return targets;
	}
}

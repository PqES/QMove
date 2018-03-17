package qmove.metrics.qmood.properties;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class NumberOfMethods {

	public static double calcule(IType type) {
		double nom;
		try {
			IMethod[] methods = type.getMethods();
			nom = methods.length;
			return nom;
		} catch (JavaModelException e) {
			nom = 0;
			return nom;
		}
	}
}

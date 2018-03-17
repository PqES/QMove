package qmove.metrics.qmood.properties;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class ClassInterfaceSize {

	public static double calcule(IType tipo) {
		double cis;
		try {
			IMethod[] methods = tipo.getMethods();

			int publ = 0;

			for (IMethod method : methods) {
				// exclude constructors
				if (method.isConstructor()) {
					continue;
				}
				if ((method.getFlags() & Flags.AccPublic) != 0)
					publ++;
			}
			cis = publ;
			return cis;

		} catch (JavaModelException e) {
			cis = 0;
			return cis;
		}
	}
}

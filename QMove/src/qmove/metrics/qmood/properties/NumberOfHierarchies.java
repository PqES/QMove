package qmove.metrics.qmood.properties;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class NumberOfHierarchies {

	public static double calcule(IType itype) {
		double noh;
		ITypeHierarchy hierarchy = getHierarchy(itype);
		IType[] supers = hierarchy.getAllSuperclasses(itype);
		int numSourceSupers = 0;
		for (IType type : supers) {
			if (!type.isBinary())
				numSourceSupers++;
		}
		int numSubSources = 0;
		IType[] subs = hierarchy.getSubtypes(itype); // BUG #933209
		for (IType type : subs) {
			if (!type.isBinary())
				numSubSources++;
		}
		// If the type has no source supers and any subclasses, it's considered
		// a root.
		if (numSourceSupers == 0 && numSubSources > 0)
			noh = 1;
		else
			noh = 0;
		return noh;
	}

	public static ITypeHierarchy getHierarchy(IType type) {
		ITypeHierarchy hierarchy;
		try {
			hierarchy = type.newTypeHierarchy((IJavaProject) type.getAncestor(IJavaElement.JAVA_PROJECT), null);
			return hierarchy;
		} catch (JavaModelException e) {
			return null;
		}

	}
}

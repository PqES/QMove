package qmove.metrics.qmood.properties;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class AverageNumberOfAncestors {

	public static double calcule(IType iType) {
		double ana;
		ITypeHierarchy hierarchy = getHierarchy(iType);
		IType[] supers = hierarchy.getAllSuperclasses(iType);
		ana = supers.length;
		return ana;
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

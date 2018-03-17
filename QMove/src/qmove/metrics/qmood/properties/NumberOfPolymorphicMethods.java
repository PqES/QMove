package qmove.metrics.qmood.properties;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.Checks;

@SuppressWarnings("restriction")
public class NumberOfPolymorphicMethods {

	public static double calcule(IType tipo) {
		double nop;
		try {
			IMethod[] methods = tipo.getMethods();

			int poly = 0;
			// inst - getters and setters

			for (IMethod method : methods) {
				// exclude constructors
				if (method.isConstructor()) {
					continue;
				}

				if (!tipo.isInterface() && isPolymorphic(method, getHierarchy(tipo)))
					poly++;

			}
			nop = poly;
			return nop;
		} catch (JavaModelException e) {
			nop = 0;
			return nop;
		}
	}

	private static boolean isPolymorphic(IMethod method, ITypeHierarchy hierarchy) throws JavaModelException {
		for (IType subType : hierarchy.getAllSubtypes(hierarchy.getType())) {
			IMethod found = Checks.findMethod(method, subType);
			if (found != null)
				return true;
		}
		return false;
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

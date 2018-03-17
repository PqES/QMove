package qmove.metrics.qmood.properties;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

@SuppressWarnings("restriction")
public class MeasureOfFunctionalAbstraction {

	public static double calcule(IType type) {
		double mfa;
		try {
			ITypeHierarchy hierarchy = getHierarchy(type);
			int declared = 0;
			int inherited = 0;

			declared = filterNonConstructors(hierarchy.getType().getMethods()).size();

			for (IType superType : hierarchy.getAllSuperclasses(hierarchy.getType()))
				if (superType instanceof ResolvedSourceType) {
					inherited += filterPublicAndProtected(superType.getMethods()).size();
				}
			if (declared + inherited == 0) {
				mfa = 0;
			} else {
				mfa = ((double) inherited / ((double) declared + (double) inherited));
			}

			return (double) mfa;

		} catch (JavaModelException e) {
			mfa = 0;
			return (double) mfa;
		}
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

	private static ArrayList<IMethod> filterNonConstructors(IMethod[] methods) throws JavaModelException {
		ArrayList<IMethod> filteredMethods = new ArrayList<IMethod>(methods.length);
		for (IMethod metodo : methods) {
			if (!metodo.isConstructor() && !JdtFlags.isStatic(metodo)) {
				filteredMethods.add(metodo);
			}
		}
		return filteredMethods;
	}

	private static ArrayList<IMethod> filterPublicAndProtected(IMethod[] methods) throws JavaModelException {
		ArrayList<IMethod> filteredMethods = new ArrayList<IMethod>(methods.length);
		for (IMethod metodo : methods) {
			if (!metodo.isConstructor() && !JdtFlags.isStatic(metodo)
					&& (JdtFlags.isPublic(metodo) || JdtFlags.isProtected(metodo))) {
				filteredMethods.add(metodo);
			}
		}
		return filteredMethods;
	}
}

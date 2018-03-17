package qmove.metrics.qmood.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

@SuppressWarnings("restriction")
public class CohesionAmongMethodsOfClass {

	public static double calcule(IType tipo) {
		double cam;
		try {

			IMethod[] methods = tipo.getMethods();
			ArrayList<IMethod> filteredMethods = new ArrayList<IMethod>();
			Set<String> classParameterTypes = new HashSet<String>();
			double sumIntersection = 0.0;

			// Filtering the constructors and static methods
			for (IMethod method : methods) {

				if (!method.isConstructor() && !JdtFlags.isStatic(method))
					filteredMethods.add(method);
			}

			// Calculating all the parameters on the whole class
			for (IMethod method : filteredMethods) {
				for (String pType : method.getParameterTypes()) {
					if (!pType.contains(tipo.getElementName()))
						classParameterTypes.add(pType);
				}
			}

			// If the class has methods without parameters, the metric is set to
			// the default value of 0 (maybe needs to be 1).
			if (classParameterTypes.isEmpty()) {
				cam = 0;
				return cam;
			}

			// CAM is calculated as the mean of the proportional class parameter
			// usage by the methods
			for (IMethod method : filteredMethods) {
				Set<String> parametros = new HashSet<String>(); // Set is used
																// to eliminate
																// duplicates
				for (String pType : method.getParameterTypes()) {
					if (!pType.contains(tipo.getElementName()))
						parametros.add(pType);
				}
				sumIntersection += parametros.size(); // Somat�rio da interse��o
														// dos m�todos com todos
														// os m�todos da classe
			}

			cam = sumIntersection / (filteredMethods.size() * classParameterTypes.size());
			return cam;
		} catch (JavaModelException e) {
			cam = 0;
			return cam;
		}

	}
}

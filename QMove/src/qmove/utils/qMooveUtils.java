package qmove.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import qmove.compilation.DependencyVisitor;

public class qMooveUtils {

	public static Map<String, ArrayList<IMethod>> getClassesMethods(final IProject project) throws CoreException {
		Map<String, ArrayList<IMethod>> map = new HashMap<String, ArrayList<IMethod>>();
//		IJavaProject p = je.getJavaProject();
//		IProject project = (IProject) p.getResource();
		project.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws JavaModelException {
				if (resource instanceof IFile && resource.getName().endsWith(".java")) {
					ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
					//if (unit.isOpen()){
						DependencyVisitor dp = new DependencyVisitor(unit);
						map.putAll(dp.getMapMethods());
					//}
				}
				return true;
			}
		});
		return map;
	}
	
	/*public static Collection<String> getClassNames(final IProject project) throws CoreException {
		final Collection<String> result = new LinkedList<String>();
		project.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) {
				if (resource instanceof IFile && resource.getName().endsWith(".java")) {
					ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
					final String className = getClassName(unit);
					if (unit.isOpen() && className != null){
						result.add(className);
					}
				}
				return true;
			}
		});

		//Collections.sort((List<String>) result);
		//Collections.reverse((List<String>) result);

		return result;
	}
	
	public static String getClassName(ICompilationUnit unit) {
		try {
			IPackageDeclaration packages[] = unit.getPackageDeclarations();
			String pack;
			if (packages.length > 0)
				pack = packages[0].getElementName() + ".";
			else
				pack = "";

			String clazz = unit.getElementName();
			clazz = clazz.substring(0, clazz.indexOf(".java"));

			return pack + clazz;
		} catch (JavaModelException e) {
			return null;
		}
	}
	
	public static Collection<IMethod> getMethodNames(final IProject project, final String className) throws CoreException {
		final Collection<IMethod> result = new LinkedList<IMethod>();
		project.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) {
				if (resource instanceof IMethod) {
	talvez desnecessario final String methodName = resource.getName();
	talvez desnecessarioif (className.equals(((IMethod)resource).getClassFile().getElementName()) && methodName != null){
						result.add((IMethod)resource);
					}
				}
				return true;
			}
		});

		//Collections.sort((List<String>) result);
		//Collections.reverse((List<String>) result);

		return result;
	}*/
}

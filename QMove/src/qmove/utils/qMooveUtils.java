package qmove.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import qmove.compilation.DependencyVisitor;

public class qMooveUtils {
	
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	public static void writeCsvFile(String method, double[] metrics) {
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(System.getProperty("user.home") + "/dataMetrics.csv", true);

			//Write the CSV file header
			//fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			//fileWriter.append(NEW_LINE_SEPARATOR);
			
			//Write a new student object list to the CSV file
			fileWriter.append(method);
			fileWriter.append(COMMA_DELIMITER);
			
			double avg = 0;
			
			for (int i=0; i<metrics.length; i++) {
				avg+=metrics[i];
				fileWriter.append(String.valueOf(metrics[i]));
				fileWriter.append(COMMA_DELIMITER);				
			}
			
			fileWriter.append(String.valueOf(avg/6));
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append(NEW_LINE_SEPARATOR);
					
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	public static void initializeCsvFile() {
		FileWriter fileWriter = null;
		
		try {
			fileWriter = new FileWriter(System.getProperty("user.home") + "/dataMetrics.csv", true);

			//Write the CSV file header
			//fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			//fileWriter.append(NEW_LINE_SEPARATOR);
			
			//Write a new student object list to the CSV file
			fileWriter.append("METHOD / TARGET");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("REU");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("FLE");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("EFE");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("EXT");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("FUN");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("UND");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("AVERAGE");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append(NEW_LINE_SEPARATOR);
					
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	

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

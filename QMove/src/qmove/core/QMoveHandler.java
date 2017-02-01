package qmove.core;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import qmove.movemethod.QMoodMetrics;
import qmove.movemethod.ClassMethod;
import qmove.movemethod.MethodsChosen;
import qmove.movemethod.MoveMethod;


import org.eclipse.jface.viewers.TreeSelection;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class QMoveHandler extends AbstractHandler {

	ArrayList<ClassMethod> methods = new ArrayList<ClassMethod>();
	public static ArrayList<MethodsChosen> methodsMoved = new ArrayList<MethodsChosen>();
	AbstractMetricSource ms;
	IProject p = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			getMethods(event);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for(int i=0; i<methods.size(); i++){
//			System.out.println("Class "+methods.get(i).getClassName());
//			System.out.println("Methods:");
//			for(int j=0; j<methods.get(i).getMethods().length; j++)
//				System.out.println(methods.get(i).getMethods()[j]);
//		}
		
		Metric[] metricsOriginal = QMoodMetrics.getQMoodMetrics(ms);
		MoveMethod checkMove = new MoveMethod(event, metricsOriginal);
		//ArrayList<MethodsChosen> methodsMoved = new ArrayList<MethodsChosen>();
		MethodsChosen aux;
	    for(int i=0; i<methods.size(); i++){
	    	//System.out.println("Package: "+methods.get(i).getPackageName()+" Class "+methods.get(i).getClassName());
			
	    		aux = checkMove.startRefactoring(methods.get(i));
	    		if(aux != null) methodsMoved.add(aux);
	    		
	    	//Map<String, ArrayList<IMethod>> mapMethods = new HashMap<String, ArrayList<IMethod>>();
		    //mapMethods = qMooveUtils.getClassesMethods(p);
			//if(aux != null) methodsChosen.add(aux);		
						
	    }
	    
	    
	    try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("qmove.views.QMoveView");
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	     
	   FileWriter arq;
		try {
			arq = new FileWriter("C:\\Users\\Public\\Documents\\results.txt");
			PrintWriter gravarArq = new PrintWriter(arq);
	    	gravarArq.printf("Method/Actual Class                                       			Class Moved\n");
	    	for(int i=0;i < methodsMoved.size(); i++){
	    		gravarArq.printf("%s.%s.%s             %s\n", 
	    				methodsMoved.get(i).getpackageOriginal(),
	    				methodsMoved.get(i).getClassOriginal(),
	    				methodsMoved.get(i).getMethod().getElementName(),
						methodsMoved.get(i).getTargetChosen().getName());	
		    }
	    	
	    	arq.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
//		TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
//	    IJavaElement je = (IJavaElement) selection.getFirstElement();
	    //AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
		
	  //  Metric[] metricsOriginal = QMoodMetrics.getQMoodMetrics(event);
		
//		try {
//			getAllMethods(je, event, metricsOriginal);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		return null;
	}
	
	public void getMethods(ExecutionEvent event) throws JavaModelException{
		TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
	    IJavaElement je = (IJavaElement) selection.getFirstElement();
	    ms = Dispatcher.getAbstractMetricSource(je);
		IJavaProject project = je.getJavaProject();
	    p = (IProject)project.getResource();
		if (project.isOpen()) {
		      IPackageFragment[] packages = project.getPackageFragments();
		      // parse(JavaCore.create(project));
		      for (IPackageFragment mypackage : packages) {
		        if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
		          for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
		             IType[] types = unit.getTypes();
		             for (int i = 0; i < types.length; i++) {
		               IType type = types[i];
		               IMethod[] imethods = type.getMethods();
		               for(int j=0; j<imethods.length; j++)
		            	   methods.add(new ClassMethod(mypackage.getElementName(), type.getElementName(), imethods[j]));
		             }
		          }
		        }
		      }
		}
		
	}
	
	/*public void getAllMethods(IJavaElement je, ExecutionEvent event, Metric[] metricsOriginal) throws CoreException, IOException, OperationCanceledException, InterruptedException{
		
		IJavaProject jproj = je.getJavaProject();
	    IProject p = (IProject)jproj.getResource();
	    
	    if(!p.isOpen()){
	    	p.open(null);
	    }
	    
	    Map<String, ArrayList<IMethod>> mapMethods = new HashMap<String, ArrayList<IMethod>>();
	    
	    mapMethods = qMooveUtils.getClassesMethods(p);
		
	    MoveMethod checkMove = new MoveMethod();
	    
	    ArrayList<MethodsChosen> methodsChosen = new ArrayList<MethodsChosen>();
	    MethodsChosen aux;
	 
	    
	    for (Map.Entry<String, ArrayList<IMethod>> entrada : mapMethods.entrySet()) {
	    	
	       	System.out.println(entrada.getKey());
	       
	       	   	for(int i=0; i<entrada.getValue().size(); i++){
	       		System.out.println(entrada.getValue().get(i));
	       		aux = checkMove.startRefactoring(entrada.getValue().get(i), event, metricsOriginal);
	       		if(aux != null) methodsChosen.add(aux); 
	       	}
		}
	    
	    FileWriter arq = new FileWriter("C:\\Users\\Public\\Documents\\results.txt");
    	PrintWriter gravarArq = new PrintWriter(arq);
    	gravarArq.printf("Method             Actual Class             Class Moved");
    	for(int i=0;i < methodsChosen.size(); i++){
	    	methodsChosen.get(i).move();
	    	gravarArq.printf("%s             %s             %s", methodsChosen.get(i).getMethod().getSignature(),
	    														 methodsChosen.get(i).getMethod().getClass().getName(),
	    														 methodsChosen.get(i).getTargetChosen().getClass().getName());
	    }
    	
    	arq.close();
	}*/
}

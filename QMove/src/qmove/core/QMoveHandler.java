package qmove.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import qmove.movemethod.QMoodMetrics;
import qmove.movemethod.Recommendation;
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
	ArrayList<MethodsChosen> methodsMoved = new ArrayList<MethodsChosen>();
	public static ArrayList<Recommendation> listRecommendations = new ArrayList<Recommendation>();
	Metric[] metricsOriginal;
	ArrayList<ClassMethod> methodsCanBeMoved = new ArrayList<ClassMethod>();
	AbstractMetricSource ms;
	IJavaElement jee;
	IProject p = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			getMethods(event);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		ms = Dispatcher.getAbstractMetricSource(jee);
	    metricsOriginal = QMoodMetrics.getQMoodMetrics(ms);
		MoveMethod checkMove = new MoveMethod(jee);

		
		for(int i=0; i<methods.size(); i++){
			try {
				if(checkMove.ckeckIfMethodCanBeMoved(methods.get(i)))
						methodsCanBeMoved.add(methods.get(i));
			} catch (OperationCanceledException | CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		MethodsChosen aux;
	    
		while(methodsCanBeMoved.size() > 0){
			
			for(int i=0; i<methodsCanBeMoved.size(); i++){
	    	
				aux = checkMove.startRefactoring(methodsCanBeMoved.get(i), metricsOriginal);
				if(aux != null) methodsMoved.add(aux);		
			}
			
			Metric[] auxMetrics = metricsOriginal;
			
			Collections.sort (methodsMoved, new Comparator() {
	            public int compare(Object o1, Object o2) {
	                MethodsChosen m1 = (MethodsChosen) o1;
	                MethodsChosen m2 = (MethodsChosen) o2;
	                return m1.calculePercentage(auxMetrics) > m2.calculePercentage(auxMetrics) ? -1 : (m1.calculePercentage(auxMetrics) < m2.calculePercentage(auxMetrics) ? +1 : 0);
	            }
	        });
			
			
			metricsOriginal = methodsMoved.get(0).getMetrics();
			
			try {
				methodsMoved.get(0).move();
			} catch (OperationCanceledException | CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			methodsCanBeMoved.removeIf(methodsCanBeMoved -> methodsCanBeMoved.getMethod() == methodsMoved.get(0).getMethod());
			
			listRecommendations.add(new Recommendation (methodsMoved.get(0), methodsMoved.get(0).calculePercentage(auxMetrics)));
			
			methodsMoved.removeAll(methodsMoved);
			
			
		}
	    
	    
	    try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("qmove.views.QMoveView");
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    
	     
	   /*FileWriter arq;
		try {
			arq = new FileWriter("C:\\Users\\Public\\Documents\\results.txt");
			PrintWriter gravarArq = new PrintWriter(arq);
	    	for(int i=0;i < methodsMoved.size(); i++){
	    		gravarArq.printf("Method: %s.%s.%s\nTo: %s.%s\nIncrease: %s\n\n", 
	    				listRecommendations.get(i).getPackageMethodName(),
	    				listRecommendations.get(i).getClassMethodName(),
	    				listRecommendations.get(i).getMethodName(),
	    				listRecommendations.get(i).getPackageTargetName(),
	    				listRecommendations.get(i).getClassTargetName(),
	    				listRecommendations.get(i).getIncrease());
		    }
	    	
	    	arq.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		IProgressMonitor m = new NullProgressMonitor();
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = workspaceRoot.getProject("Temp");
	    try {
			project.delete(true, m);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
		
		
		return null;
	}
	
	public void getMethods(ExecutionEvent event) throws CoreException, InterruptedException{
		TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
	    IJavaElement je = (IJavaElement) selection.getFirstElement();
//	    ms = Dispatcher.getAbstractMetricSource(je);
		IJavaProject project = je.getJavaProject();
	    p = (IProject)project.getResource();
	    IJavaProject projectTemp = JavaCore.create(cloneProject());
	    Thread.sleep(1000);
	    jee = projectTemp.getPrimaryElement();
	    Thread.sleep(1000);
	    if (projectTemp.isOpen()) {
	    	IPackageFragment[] packages = projectTemp.getPackageFragments();
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
	
	public IProject cloneProject() throws CoreException{
		IProgressMonitor m = new NullProgressMonitor();
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    IProjectDescription projectDescription = p.getDescription();
	    String cloneName = "Temp";
	    // create clone project in workspace
	    IProjectDescription cloneDescription = workspaceRoot.getWorkspace().newProjectDescription(cloneName);
	    // copy project files
	    p.copy(cloneDescription, true, m);
	    IProject clone = workspaceRoot.getProject(cloneName);
	    
	    cloneDescription.setNatureIds(projectDescription.getNatureIds());
	    cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
	    cloneDescription.setDynamicReferences(projectDescription.getDynamicReferences());
	    cloneDescription.setBuildSpec(projectDescription.getBuildSpec());
	    cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
	    clone.setDescription(cloneDescription, null);
	    clone.open(m);
	    return clone;
	}
	
	/*public  IProject copyProject(String projectName) throws CoreException {
	    IProgressMonitor m = new NullProgressMonitor();
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = workspaceRoot.getProject(projectName);
	    IProjectDescription projectDescription = project.getDescription();
	    String cloneName = "Temp";
	    // create clone project in workspace
	    IProjectDescription cloneDescription = workspaceRoot.getWorkspace().newProjectDescription(cloneName);
	    // copy project files
	    project.copy(cloneDescription, true, m);
	    IProject clone = workspaceRoot.getProject(cloneName);
	    // copy the project properties
	    cloneDescription.setNatureIds(projectDescription.getNatureIds());
	    cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
	    cloneDescription.setDynamicReferences(projectDescription.getDynamicReferences());
	    cloneDescription.setBuildSpec(projectDescription.getBuildSpec());
	    cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
	    clone.setDescription(cloneDescription, null);
	    return clone;
	}*/
	
}

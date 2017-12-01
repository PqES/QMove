package qmove.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import net.sourceforge.metrics.properties.MetricsPropertyPage;
import net.sourceforge.metrics.ui.MetricsView;
import qmove.movemethod.QMoodMetrics;
import qmove.movemethod.Recommendation;
import qmove.utils.SingletonNullProgressMonitor;
import qmove.utils.qMooveUtils;
import qmove.compilation.AllMethods;
import qmove.movemethod.ClassMethod;
import qmove.movemethod.MethodsChosen;
import qmove.movemethod.MethodsTable;
import qmove.movemethod.MoveMethod;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;

public class QMoveHandler extends AbstractHandler {

	ArrayList<ClassMethod> methods = new ArrayList<ClassMethod>();
	ArrayList<MethodsChosen> methodsMoved = new ArrayList<MethodsChosen>();
	public static ArrayList<Recommendation> listRecommendations = new ArrayList<Recommendation>();
	MethodsTable methodsTable;
	double[] metricsOriginal;
	ArrayList<ClassMethod> methodsCanBeMoved = new ArrayList<ClassMethod>();
	AbstractMetricSource ms;
	IJavaElement jeCopy;
	public static IProject iProject = null;
	public static IJavaProject jproject;
	ArrayList<IMethod> iMethod = new ArrayList<IMethod>();
	Map<String, ArrayList<IMethod>> allMethods;
	public static boolean queueIsZero;
	public static boolean isOver = false;
	MethodsChosen bestMethod;
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		hideViewIfOpen();
		
		iProject = getProjectFromWorkspace(event);
		
		
		
	
			
		try {
			System.out.println("Lendo projeto: "+iProject.getName());
			allMethods = qMooveUtils.getClassesMethods(iProject);
			System.out.println("Projeto lido com sucesso!");
			System.out.println("Clonando projeto "+iProject.getName());
			getMethodsClone();
			System.out.println("Projeto clonado com sucesso!");
			
		} catch (JavaModelException e) {	
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {	
			e.printStackTrace();
		}
		
		
		System.out.print("Calculando metricas do estado atual de "+iProject.getName()+"... ");
		
		queueIsZero = false;
		while(queueIsZero == false){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Pronto!");
		
		ms = Dispatcher.getAbstractMetricSource(jeCopy);
		System.out.println("Valores das metricas atuais:");
		metricsOriginal = QMoodMetrics.getMetrics(ms);
		qMooveUtils.initializeCsvFile();
		
	    
	    
		MoveMethod checkMove = new MoveMethod(jeCopy);

		System.out.print("Verificando metodos que podem ser movidos: ");
		for(int i=0; i<methods.size(); i++){
			try {
				if(checkMove.ckeckIfMethodCanBeMoved(methods.get(i)))
						methodsCanBeMoved.add(methods.get(i));
			} catch (OperationCanceledException | CoreException e) {
				e.printStackTrace();
			}	
		}
		
		MethodsChosen aux;
		int qmoveID = 0;
		
		
		System.out.println(methodsCanBeMoved.size());
	    
		while(methodsCanBeMoved.size() > 0){
			qMooveUtils.writeCsvFile("Current", metricsOriginal);
			
			
			for(int i=0; i<methodsCanBeMoved.size(); i++){
				System.out.println("Método "+(i+1)+" de "+methodsCanBeMoved.size());
				aux = checkMove.startRefactoring(methodsCanBeMoved.get(i), metricsOriginal);
				if(aux != null) methodsMoved.add(aux);		
			}
			
			if(methodsMoved.size() == 0){
				methodsCanBeMoved.clear();
				continue;
			}
			
			double[] auxMetrics = metricsOriginal;
			
			Collections.sort (methodsMoved, new Comparator() {
	            public int compare(Object o1, Object o2) {
	                MethodsChosen m1 = (MethodsChosen) o1;
	                MethodsChosen m2 = (MethodsChosen) o2;
	                return m1.getSumMetrics(auxMetrics) > m2.getSumMetrics(auxMetrics) ? -1 : (m1.getSumMetrics(auxMetrics) < m2.getSumMetrics(auxMetrics) ? +1 : 0);
	            }
			});
			
			bestMethod = methodsMoved.get(0);
			
			
			/*ArrayList<MethodsChosen> clone = new ArrayList<MethodsChosen>(methodsMoved.size());
			    for (MethodsChosen item : methodsMoved){
					try {
						clone.add(item.clone());
					} catch (CloneNotSupportedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
			
			methodsTable = new MethodsTable(auxMetrics, clone);*/
		
			
			metricsOriginal = bestMethod.getMetrics();
			
			try {
				bestMethod.move();
			} catch (OperationCanceledException | CoreException e) {
				e.printStackTrace();
			}
			
			
			qMooveUtils.writeRecInFile(++qmoveID,
					bestMethod.getMethod().getCompilationUnit().getParent().getElementName()+"."
					+ bestMethod.getMethod().getDeclaringType().getElementName()+"::"
					+ bestMethod.getMethod().getElementName(),
					bestMethod.getTargetChosen().getType().getPackage().getName()+"."
					+ bestMethod.getTargetChosen().getType().getName(), 
					bestMethod.calculePercentage(auxMetrics));

			
			methodsCanBeMoved.removeIf(methodsCanBeMoved -> methodsCanBeMoved.getMethod() == bestMethod.getMethod());
			
			//listRecommendations.add(new Recommendation (qmoveID, methodsTable, methodsMoved.get(0), methodsMoved.get(0).calculePercentage(auxMetrics), getMethod(methodsMoved.get(0).getMethod())));
			
			methodsMoved.removeAll(methodsMoved);
			
			bestMethod = null;
			
			
		}
	            
	    
	    try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("qmove.views.QMoveView");
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    
	    IProgressMonitor m = new NullProgressMonitor();
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = workspaceRoot.getProject(jeCopy.getJavaProject().getProject().getName());
	    try {
			project.delete(true, m);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	   
		return null;
	}
	
	private void hideViewIfOpen(){
		IWorkbenchPage wp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		//Find desired view :
		IViewPart myView=wp.findView("qmove.views.QMoveView");

		//Hide the view :
		wp.hideView(myView);
		
	}
	
	private IProject getProjectFromWorkspace(ExecutionEvent event){
		
		TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
		 
		if (selection == null || selection.getFirstElement() == null) {
            // Nothing selected, do nothing
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information", "Please select a project");
            return null;
        }
		
		JavaProject jp;
		Project p;
		
		try{
			jp = (JavaProject) selection.getFirstElement();
			return jp.getProject();
		} catch(ClassCastException e){
			p = (Project) selection.getFirstElement();
			return p.getProject();
		}
	}
	
	public void getMethodsProject(IJavaElement je) throws JavaModelException {
		IJavaProject project = je.getJavaProject();
	    iProject = (IProject)project.getResource();
	    if (project.isOpen()) {
	    	IPackageFragment[] packages = project.getPackageFragments();
		      for (IPackageFragment mypackage : packages) {
		        if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
		          for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
		             IType[] types = unit.getTypes();
		             for (int i = 0; i < types.length; i++) {
		               IType type = types[i];
		               IMethod[] imethods = type.getMethods();
		               for(int j=0; j<imethods.length; j++)
		            	   iMethod.add(imethods[j]);
		             }
		          }
		        }
		      }
	    }
	}
		
	
	
	public void getMethodsClone() throws CoreException, InterruptedException{
		
		IJavaProject projectTemp = JavaCore.create(cloneProject());
	    //Thread.sleep(10000);
	    jeCopy = projectTemp.getPrimaryElement();
	   // if (projectTemp.isOpen()) {
	    	IPackageFragment[] packages = projectTemp.getPackageFragments();
		      for (IPackageFragment mypackage : packages) {
		        if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
		          for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
		             IType[] types = unit.getTypes();
		             for (int i = 0; i < types.length; i++) {
		               IType type = types[i];
		               IMethod[] imethods = type.getMethods();
		               for(int j=0; j<imethods.length; j++)
		            	   methods.add(new ClassMethod(mypackage, type, imethods[j]));
		             }
		          }
		        }
		      }
		//}
		
	}
	
	public IProject cloneProject() throws CoreException{
		IProgressMonitor m = new NullProgressMonitor();
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    IProjectDescription projectDescription = iProject.getDescription();
	    String cloneName = iProject.getName()+"Temp";
	    // create clone project in workspace
	    IProjectDescription cloneDescription = workspaceRoot.getWorkspace().newProjectDescription(cloneName);
	    // copy project files
	    iProject.copy(cloneDescription, true, m);
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
	
	public IMethod getMethod(IMethod method){
		
		String methodOriginal;
		String classOriginal;
		String methodClone = method.getElementName();
		String classClone = method.getCompilationUnit().getParent().getElementName() + "." + method.getDeclaringType().getElementName();
		
		try{
			for (Map.Entry<String, ArrayList<IMethod>> entrada : allMethods.entrySet()) {
				
				classOriginal = entrada.getKey();
				
				for(int i=0; i<entrada.getValue().size(); i++){
					
					methodOriginal = entrada.getValue().get(i).getElementName();
					
					if(classClone.compareTo(classOriginal) == 0
						&& methodClone.compareTo(methodOriginal) == 0){
					
						return entrada.getValue().get(i);
					}
				}
			}
		} catch(NullPointerException n){
			System.out.println(n.getMessage());
		}
		
		return null;
		
	}
	 
}




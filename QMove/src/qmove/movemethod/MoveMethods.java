package qmove.movemethod;
//MÉTODO moveMethods() RETORNA UM ARRAYLIST DE RECOMMENDATION, VER SE REALMENTE PRECISA DISSO
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
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
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import qmove.utils.qMooveUtils;

public class MoveMethods {
	
	ArrayList<ClassMethod> methods;
	ArrayList<ClassMethod> methodsCanBeMoved;
	ArrayList<MethodsChosen> methodsMoved;
	ArrayList<Recommendation> listRecommendations, newListRecommendations;
	ArrayList<IMethod> iMethod;
	Map<String, ArrayList<IMethod>> allMethods;
	MethodsTable methodsTable;
	Metric[] metricsOriginal;
	AbstractMetricSource ms;
	IJavaElement jee;
	IJavaProject project;
	ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	MethodMetric candidateChosen;
	
	
	public MoveMethods(IJavaProject project, ArrayList<Recommendation> listRecommendations){
		this.project = project;
		methods = new ArrayList<ClassMethod>();
		methodsMoved = new ArrayList<MethodsChosen>();
		this.listRecommendations = listRecommendations;
		methodsCanBeMoved = new ArrayList<ClassMethod>();
		iMethod = new ArrayList<IMethod>();
		newListRecommendations = new ArrayList<Recommendation>();
	}
	
	public ArrayList<Recommendation> moveMethods() throws ExecutionException {
			
		try {
			allMethods = qMooveUtils.getClassesMethods(project.getProject());
			getMethodsClone(project); 
		} catch (CoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		ms = Dispatcher.getAbstractMetricSource(jee);
	    metricsOriginal = QMoodMetrics.getQMoodMetrics(ms);
		
	    
	    		
		MethodsChosen aux;
		int qmoveID = 0;
	    
		while(listRecommendations.size() > 0){
			
			for(int i=0; i<listRecommendations.size();i++){
		    	try {
					aux = moveAndRecalculeMetrics(listRecommendations.get(i));
					if(aux != null) methodsMoved.add(aux);	
				} catch (OperationCanceledException | CoreException | InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
	    			
			Metric[] auxMetrics = metricsOriginal;
			
			Collections.sort (methodsMoved, new Comparator() {
	            public int compare(Object o1, Object o2) {
	                MethodsChosen m1 = (MethodsChosen) o1;
	                MethodsChosen m2 = (MethodsChosen) o2;
	                return m1.calculePercentage(auxMetrics) > m2.calculePercentage(auxMetrics) ? -1 : (m1.calculePercentage(auxMetrics) < m2.calculePercentage(auxMetrics) ? +1 : 0);
	            }
	        });
			
			ArrayList<MethodsChosen> clone = new ArrayList<MethodsChosen>(methodsMoved.size());
			    for (MethodsChosen item : methodsMoved){
					try {
						clone.add(item.clone());
					} catch (CloneNotSupportedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
			
			methodsTable = new MethodsTable(auxMetrics, clone);
			
			metricsOriginal = methodsMoved.get(0).getMetrics();
			
			try {
				methodsMoved.get(0).move();
			} catch (OperationCanceledException | CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			listRecommendations.removeIf(listRecommendations -> listRecommendations.getMethod().getElementName().compareTo(methodsMoved.get(0).getMethod().getElementName()) == 0);
			
			newListRecommendations.add(new Recommendation (++qmoveID, methodsTable, methodsMoved.get(0), methodsMoved.get(0).calculePercentage(auxMetrics), getMethod(methodsMoved.get(0).getMethod())));
			
			methodsMoved.removeAll(methodsMoved);
			
			
		}
	            
	    
	    try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("qmove.views.QMoveView");
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    
	    IProgressMonitor m = new NullProgressMonitor();
	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = workspaceRoot.getProject("Temp");
	    try {
			project.delete(true, m);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		return newListRecommendations;
	}
		
	
	public void getMethodsClone(IJavaProject project) throws CoreException, InterruptedException{
	    IJavaProject projectTemp = JavaCore.create(cloneProject(project.getProject()));
	    Thread.sleep(10000);
	    jee = projectTemp.getPrimaryElement();
	    if (projectTemp.isOpen()) {
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
		}
		
	}
	
	public IProject cloneProject(IProject p) throws CoreException{
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
	
	public IMethod getMethod(IMethod method){
		
		String methodOriginal;
		String classOriginal;
		String methodClone = method.getElementName();
		String classClone = method.getCompilationUnit().getParent().getElementName() + "." + method.getDeclaringType().getElementName();
		
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
		
		return null;
		
	}
	
	public ClassMethod getMethodClone(IMethod method){
		
		String methodOriginal;
		String classOriginal;
		String methodClone = method.getElementName();
		String classClone = method.getCompilationUnit().getParent().getElementName() + "." + method.getDeclaringType().getElementName();
		
		for (int i=0; i< methods.size(); i++) {
			
			classOriginal = methods.get(i).getMethod().getCompilationUnit().getParent().getElementName() + "." + methods.get(i).getClassName().getElementName();
			methodOriginal = methods.get(i).getMethod().getElementName();
				
				if(classClone.compareTo(classOriginal) == 0
					&& methodClone.compareTo(methodOriginal) == 0){
				
					return methods.get(i);
				}
		}
		
		return null;
		
	}
	
	public MethodsChosen moveAndRecalculeMetrics(Recommendation r) throws OperationCanceledException, CoreException, InterruptedException{
		
		ClassMethod classMethod = getMethodClone(r.getMethod());
		IMethod method = classMethod.getMethod();
		
		MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
				JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

		processor2.checkInitialConditions(new NullProgressMonitor());
		
		IVariableBinding[] potential = processor2.getPossibleTargets();
		
		IVariableBinding candidate = null;
		
		for(int i=0; i<potential.length; i++){
		
			processor2.setTarget(r.getTarget());
			processor2.setInlineDelegator(true);
			processor2.setRemoveDelegator(true);
			processor2.setDeprecateDelegates(false);
	
			Refactoring refactoring2 = new MoveRefactoring(processor2);
			refactoring2.checkInitialConditions(new NullProgressMonitor());
			
			RefactoringStatus status2 = refactoring2.checkAllConditions(new NullProgressMonitor());
			if (status2.getSeverity() != RefactoringStatus.OK) return null;
		
			final CreateChangeOperation create2 = new CreateChangeOperation(
						new CheckConditionsOperation(refactoring2,
						CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);
			
			PerformChangeOperation perform2 = new PerformChangeOperation(create2);
		
			IWorkspace workspace2 = ResourcesPlugin.getWorkspace();
			workspace2.run(perform2, new NullProgressMonitor());
			
			Thread.sleep(1000);
			
			ms = Dispatcher.getAbstractMetricSource(jee);
			Metric[] metricsModified = QMoodMetrics.getQMoodMetrics(ms); //calcula as metricas apos mover mï¿½todo
			
			Change undoChange = perform2.getUndoChange();
			undoChange.perform(new NullProgressMonitor()); //move o mï¿½todo para a classe original
			RefactoringStatus conditionCheckingStatus = create2.getConditionCheckingStatus();
			
			Thread.sleep(1000);
			
			if(!checkIfSomeMetricDecrease(metricsModified)){
				System.out.println("Nenhuma métrica piora");
				if(checkIfSomeMetricIncrease(metricsModified)){
					System.out.println("Pelo menos uma métrica melhora");
					potentialFiltred.add(new MethodMetric(potential[i], metricsModified));
					continue;
				}
			}
			else {
				return null;
			}
			
			
		}
		
		
		if(choosePotential()) return new MethodsChosen(classMethod, candidateChosen.getPotential(), candidateChosen.getMetrics());
		
		else return null;
			
	}
	
	
	public boolean checkIfSomeMetricDecrease(Metric[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i].getValue() - metricsOriginal[i].getValue();
			if(aux < 0) {
				return true;
			}
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) < 0) return true;
		}
		
		return false;
	}
	
	public boolean checkIfSomeMetricIncrease(Metric[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i].getValue() - metricsOriginal[i].getValue();
			if(aux > 0) {
				return true;
			}
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) > 0) return true;
		}
		
		return false;
	}
	
	
	
	public boolean choosePotential() throws OperationCanceledException, CoreException{
		
		if(potentialFiltred.size() == 0 || potentialFiltred == null) return false;
		
		if(potentialFiltred.size() == 1) {
			candidateChosen = potentialFiltred.get(0);
			return true;
		}
		
		else {
	    
			candidateChosen = potentialFiltred.get(0);
			
			for(int i=1; i<potentialFiltred.size(); i++){
			
				if(potentialFiltred.get(i).getIncreasedMetricsSum(metricsOriginal)
						> candidateChosen.getIncreasedMetricsSum(metricsOriginal))
				candidateChosen = potentialFiltred.get(i);	
			}
				
				
			return true;
		}
		
	}
}

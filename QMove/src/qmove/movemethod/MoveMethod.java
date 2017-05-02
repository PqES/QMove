package qmove.movemethod;

import java.util.ArrayList;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ui.handlers.HandlerUtil;



import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;

@SuppressWarnings("restriction")
public class MoveMethod {
	
	/*
	Job job = new Job("My Job") {
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
    	
    	return Status.OK_STATUS;
    }
	};

	// Start the Job
	job.schedule();

	job.join();
	*/
		
	private ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	private MethodMetric candidateChosen;
	private Metric[] metricsOriginal;
	private IJavaElement je;
	
	public MoveMethod(IJavaElement je){
		this.je = je;
	}
	
	
	public boolean ckeckIfMethodCanBeMoved(ClassMethod method) throws OperationCanceledException, CoreException{
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method.getMethod(),
				JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
		
		IVariableBinding[] potential = processor.getPossibleTargets();
        
		if(potential.length == 0 || potential == null) return false;
		
		else return true; 
		
		
		
	}
	
	

	
	public MethodsChosen startRefactoring(ClassMethod method, Metric[] metricsOriginal){
		
		this.metricsOriginal = metricsOriginal;
		
		MethodsChosen methodMoved = null;
		
			try {
				if(canMove(method.getMethod()))
					methodMoved = new MethodsChosen(method, candidateChosen.getPotential(), candidateChosen.getMetrics());
			} catch (OperationCanceledException | CoreException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return methodMoved;
	}
	
	public boolean canMove(IMethod method) throws OperationCanceledException, CoreException, InterruptedException {
		
		
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
				JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
		
		IVariableBinding[] potential = processor.getPossibleTargets();
        
		if(potential.length == 0 || potential == null) return false;
		
		System.out.println("-----------Metodo: "+method.getElementName()+"----------------" );
		
		IVariableBinding candidate;
		
		for (int i = 0; i < potential.length; i++) {
			
			
			MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor2.checkInitialConditions(new NullProgressMonitor());
			
		
			candidate = potential[i];
			
			System.out.println("Calculando refctoring para "+candidate.getJavaElement().getPrimaryElement().getElementName());
			
			processor2.setTarget(candidate);
			processor2.setInlineDelegator(true);
			processor2.setRemoveDelegator(true);
			processor2.setDeprecateDelegates(false);

			Refactoring refactoring = new MoveRefactoring(processor2);
			refactoring.checkInitialConditions(new NullProgressMonitor());

		
			//RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
			//if (status.getSeverity() != RefactoringStatus.OK) return false;
			//TODO Verificar o porque do status não estar OK 
			
			final CreateChangeOperation create = new CreateChangeOperation(new CheckConditionsOperation(refactoring,
						CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);
			
			PerformChangeOperation perform = new PerformChangeOperation(create);
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(perform, new NullProgressMonitor()); //move o m�todo para calcular m�tricas
			
			
		
			
			Thread.sleep(1000);
//			TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
//		    IJavaElement je = (IJavaElement) selection.getFirstElement();
			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
			Metric[] metricsModified = QMoodMetrics.getQMoodMetrics(ms); //calcula as metricas apos mover m�todo
			Change undoChange = perform.getUndoChange();
			undoChange.perform(new NullProgressMonitor()); //move o m�todo para a classe original
			//RefactoringStatus conditionCheckingStatus = create.getConditionCheckingStatus();
			Thread.sleep(1000);
			
			if(!checkIfSomeMetricDecrease(metricsModified)){
				System.out.println("Nenhuma métrica piora");
				if(checkIfSomeMetricIncrease(metricsModified)){
					System.out.println("Pelo menos uma métrica melhora");
					potentialFiltred.add(new MethodMetric(potential[i], metricsModified));
					continue;
				}
				else System.out.println("Nenhuma métrica melhora");
			}
			else {
				System.out.println("Alguma métrica piora");
				return false;
			}
			
			
		}
		
		if(choosePotential()){
			potentialFiltred.clear();
			return true;
		}
		
		else return false;
			
		
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

/*class RunWorkspace implements Runnable {
    
	PerformChangeOperation perform;
	
	public RunWorkspace(PerformChangeOperation perform){
		this.perform = perform;
	}
	
	public void run() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			workspace.run(perform, new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //move o m�todo para calcular m�tricas
		
    }
}*/
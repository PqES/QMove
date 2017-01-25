package qmove.movemethod;

import java.util.ArrayList;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
import net.sourceforge.metrics.ui.QMoodMetrics;

@SuppressWarnings("restriction")
public class MoveMethod {
		
	private ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	private MethodMetric candidateChosen;

	
	public MethodsChosen startRefactoring(IMethod method, ExecutionEvent event, Metric[] metricsOriginal) throws OperationCanceledException, CoreException, InterruptedException {
		
		
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
				JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
		
		IVariableBinding[] potential = processor.getPossibleTargets();
        
		
		if(potential.length == 0 || potential == null) return null;
		
		IVariableBinding candidate;
		
		for (int i = 0; i < potential.length; i++) {
		
			candidate = potential[i];
			
			processor.setTarget(candidate);
			processor.setInlineDelegator(true);
			processor.setRemoveDelegator(true);
			processor.setDeprecateDelegates(false);

			Refactoring refactoring = new MoveRefactoring(processor);
			refactoring.checkInitialConditions(new NullProgressMonitor());

		
			RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
			if (status.getSeverity() != RefactoringStatus.OK) return null;

			final CreateChangeOperation create = new CreateChangeOperation(new CheckConditionsOperation(refactoring,
						CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);
			
			PerformChangeOperation perform = new PerformChangeOperation(create);
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(perform, new NullProgressMonitor()); //move o m�todo para calcular m�tricas
			
			Thread.sleep(1000);
			TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
		    IJavaElement je = (IJavaElement) selection.getFirstElement();
		    AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
			Metric[] metricsModified = QMoodMetrics.getQMoodMetrics(ms); //calcula as metricas apos mover m�todo
			
			
			Change undoChange = perform.getUndoChange();
			undoChange.perform(new NullProgressMonitor()); //move o m�todo para a classe original
			//RefactoringStatus conditionCheckingStatus = create.getConditionCheckingStatus();
			
			if(checkIfSomeMetricDecrease(metricsOriginal, metricsModified)) continue;
		    
		    if(!checkIfSomeMetricIncrease(metricsOriginal, metricsModified)) continue;
			
			potentialFiltred.add(new MethodMetric(potential[i], metricsModified));
		}
		
		if(choosePotential(metricsOriginal)){ //moveMethodToTargetChosen(candidateChosen.getPotential(), processor);
			MethodsChosen methodTarget = new MethodsChosen(method, candidateChosen.getPotential());
			return methodTarget;
		}
		
		else return null;
		
	}
	
	public boolean checkIfSomeMetricDecrease(Metric[] metricsOriginal, Metric[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i].getValue() - metricsOriginal[i].getValue();
			System.out.println(aux);
			if(aux < 0) return true;
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) < 0) return true;
		}
		
		return false;
	}
	
	public boolean checkIfSomeMetricIncrease(Metric[] metricsOriginal, Metric[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i].getValue() - metricsOriginal[i].getValue();
			System.out.println(aux);
			if(aux > 0) return true;
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) > 0) return true;
		}
		
		return false;
	}
	
	
	
	public boolean choosePotential(Metric[] metricsOriginal) throws OperationCanceledException, CoreException{
		
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
		
	
	
	
/*	public void moveMethodToTargetChosen(IVariableBinding targetChosen, MoveInstanceMethodProcessor processor2) throws OperationCanceledException, CoreException{
		        
			processor2.setTarget(targetChosen);
			processor2.setInlineDelegator(true);
			processor2.setRemoveDelegator(true);
			processor2.setDeprecateDelegates(false);
	
			Refactoring refactoring2 = new MoveRefactoring(processor2);
			refactoring2.checkInitialConditions(new NullProgressMonitor());
			
			RefactoringStatus status2 = refactoring2.checkAllConditions(new NullProgressMonitor());
			if (status2.getSeverity() != RefactoringStatus.OK) return;
		
			final CreateChangeOperation create2 = new CreateChangeOperation(
						new CheckConditionsOperation(refactoring2,
						CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);
			
			PerformChangeOperation perform2 = new PerformChangeOperation(create2);
		
			IWorkspace workspace2 = ResourcesPlugin.getWorkspace();
			workspace2.run(perform2, new NullProgressMonitor());
	}*/
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
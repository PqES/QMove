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
public class MoveMethodExample {
	
	private MoveInstanceMethodProcessor processor;
	private IVariableBinding candidate;
	private IVariableBinding[] potential;
	private ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	private MethodMetric candidateChosen;
	private Refactoring refactoring;
	private RefactoringStatus status;
	private CreateChangeOperation create;
	private PerformChangeOperation perform;
	private IWorkspace workspace;
	private Change undoChange;
	private TreeSelection selection;
    private IJavaElement je;
    private AbstractMetricSource mss;

	public void startRefactoring(IMethod method, AbstractMetricSource ms, ExecutionEvent event, Metric[] metricsOriginal) throws OperationCanceledException, CoreException {
		
		
		processor = new MoveInstanceMethodProcessor(method,
				JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
        
		if(processor.getPossibleTargets().length == 0) {
			potential = null;
			return;
		}
		
		potential = processor.getPossibleTargets();
		
		//if(potential.length == 0 || potential == null) return;
		
		
		moveAndCalculeMetrics(event, metricsOriginal);
		
		choosePotential(metricsOriginal, method);
		
	}
	
	public void moveAndCalculeMetrics(ExecutionEvent event, Metric[] metricsOriginal) throws OperationCanceledException, CoreException{
	
		if(potential.length == 0 || potential == null) return;
		
		for (int i = 0; i < potential.length; i++) {
		
			candidate = potential[i];
			
			processor.setTarget(candidate);
			processor.setInlineDelegator(true);
			processor.setRemoveDelegator(true);
			processor.setDeprecateDelegates(false);

			refactoring = new MoveRefactoring(processor);
			refactoring.checkInitialConditions(new NullProgressMonitor());
		
			if(refactoring == null) continue;
			
			status = refactoring.checkAllConditions(new NullProgressMonitor());
			if (status.getSeverity() != RefactoringStatus.OK) return;
	
			create = new CreateChangeOperation(new CheckConditionsOperation(refactoring,
												CheckConditionsOperation.ALL_CONDITIONS),
												RefactoringStatus.FATAL);
			
			perform = new PerformChangeOperation(create);
			workspace = ResourcesPlugin.getWorkspace();
			workspace.run(perform, new NullProgressMonitor()); //este comando aplica o refatoramento
			
			selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
		    je = (IJavaElement) selection.getFirstElement();
		    mss = Dispatcher.getAbstractMetricSource(je);
			
		    Metric[] metricsModified = QMoodMetrics.getQMoodMetrics(mss); //calcula as metricas apos mover método
		 
		 	undoChange = perform.getUndoChange();
			undoChange.perform(new NullProgressMonitor()); //move o método para a classe original
			
			if(checkIfSomeMetricDecrease(metricsOriginal, metricsModified)) continue;
		    
		    if(!checkIfSomeMetricIncrease(metricsOriginal, metricsModified)) continue;
			
			potentialFiltred.add(new MethodMetric(potential[i], metricsModified));
		}
		
	}
	
	public boolean checkIfSomeMetricDecrease(Metric[] metricsOriginal, Metric[] metricsModified){
		
		for(int i=0; i < metricsOriginal.length; i++){
			if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) < 0) return true;
		}
		
		return false;
	}
	
	public boolean checkIfSomeMetricIncrease(Metric[] metricsOriginal, Metric[] metricsModified){
		
		for(int i=0; i < metricsOriginal.length; i++){
			if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) > 0) return true;
		}
		
		return false;
	}
	
	
	public void choosePotential(Metric[] metricsOriginal, IMethod method) throws OperationCanceledException, CoreException{
		
		if(potentialFiltred.size() == 0 || potentialFiltred == null) return;
		
		if(potentialFiltred.size() == 1) moveMethodToTargetChosen(potentialFiltred.get(0).getPotential(), method);
		
		else {
	    
			candidateChosen = potentialFiltred.get(0);
			
			for(int i=1; i<potentialFiltred.size(); i++){
			
				if(potentialFiltred.get(i).getIncreasedMetricsSum(metricsOriginal)
						> candidateChosen.getIncreasedMetricsSum(metricsOriginal))
				candidateChosen = potentialFiltred.get(i);	
			}
				
				
			moveMethodToTargetChosen(candidateChosen.getPotential(), method);
		}
		
	}
		
	
	
	public void moveMethodToTargetChosen(IVariableBinding targetChosen, IMethod method) throws OperationCanceledException, CoreException{
		
		MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(
				method,JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

		processor2.checkInitialConditions(new NullProgressMonitor());
        
        
			
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
			
			
		/*
		processor.setTarget(targetChosen);
		processor.setInlineDelegator(true);
		processor.setRemoveDelegator(true);
		processor.setDeprecateDelegates(false);

		refactoring = new MoveRefactoring(processor);
		refactoring.checkInitialConditions(new NullProgressMonitor());
	
		if(refactoring == null) return;
		
		status = refactoring.checkAllConditions(new NullProgressMonitor());
		if (status.getSeverity() != RefactoringStatus.OK) return;

		create = new CreateChangeOperation(new CheckConditionsOperation(refactoring,
											CheckConditionsOperation.ALL_CONDITIONS),
											RefactoringStatus.FATAL);
		
		perform = new PerformChangeOperation(create);
		workspace = ResourcesPlugin.getWorkspace();
		workspace.run(perform, new NullProgressMonitor()); //este comando aplica o refatoramento
		*/
		
		
	}

	/*
	protected Refactoring getRefactoring(IMethod method) throws OperationCanceledException, CoreException {

		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(
				method,
				JavaPreferencesSettings.getCodeGenerationSettings(method
						.getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
        
        Refactoring refNull = null;
		IVariableBinding target = null;
		IVariableBinding[] targets = processor.getPossibleTargets();
		if (targets.length == 0) {
			return refNull;
		}
		else{
			for (int i = 0; i < targets.length; i++) {
				IVariableBinding candidate = targets[i];
				if (candidate.getName().equals("field")) {//Nome da variavel a ser usada pra movimentaÃ§Ã£o estÃ¡ como field
					target = candidate;				
					break;
				} else {
					target = candidate;
				}
			}
			
			processor.setTarget(target);
			processor.setInlineDelegator(true);
			processor.setRemoveDelegator(true);
			processor.setDeprecateDelegates(false);
	
			Refactoring ref = new MoveRefactoring(processor);
			ref.checkInitialConditions(new NullProgressMonitor());
	
			return ref;
		}
	}


	public void performRefactoring(IMethod method) throws OperationCanceledException, CoreException {
		
//		Refactoring refactoring = getRefactoring(method);
//		RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
//		if (status.getSeverity() != RefactoringStatus.OK) return false;
//		else return true;
		
		Refactoring refactoring = getRefactoring(method);

		if(refactoring == null) return;
		
		else {
			
			RefactoringStatus status = refactoring
					.checkAllConditions(new NullProgressMonitor());
			if (status.getSeverity() != RefactoringStatus.OK)
				return;
	
			final CreateChangeOperation create = new CreateChangeOperation(
					new CheckConditionsOperation(refactoring,
							CheckConditionsOperation.ALL_CONDITIONS),
					RefactoringStatus.FATAL);
			PerformChangeOperation perform = new PerformChangeOperation(create);
	
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			//este comando aplica o refatoramento
			workspace.run(perform, new NullProgressMonitor());
			
			//se precisar desfazer o refatoramento, pode usar o undo
					
			
	//		Change undoChange = perform.getUndoChange();
	//		undoChange.perform(new NullProgressMonitor());
	//				RefactoringStatus conditionCheckingStatus = create
	//						.getConditionCheckingStatus();
		}		
	}
*/	
}


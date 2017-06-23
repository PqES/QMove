package qmove.movemethod;

import java.util.ArrayList;
import java.util.Arrays;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
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
import net.sourceforge.metrics.ui.MetricsView;
import qmove.core.QMoveHandler;

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
	
	public static boolean[] flags = new boolean[6];		
	private ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	private MethodMetric candidateChosen;
	private double[] metricsOriginal;
	private IJavaElement je;
	
	public MoveMethod(IJavaElement je){
		this.je = je;
		for(int ii=0; ii < flags.length; ii++){
			flags[ii] = false;
		}
	}
	
	
	public boolean ckeckIfMethodCanBeMoved(ClassMethod method) throws OperationCanceledException, CoreException{
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method.getMethod(),
				JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
		
		IVariableBinding[] potential = processor.getPossibleTargets();
        
		if(potential.length == 0 || potential == null) return false;
		
		else return true; 
		
		
		
	}
	
	

	
	public MethodsChosen startRefactoring(ClassMethod method, double[] metricsOriginal){
		
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
			
			System.out.println("Calculando refactoring para "+candidate.getJavaElement().getPrimaryElement().getElementName());
			
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
			
			/*Job job = new Job("My Job") {
			    
			    @Override
			    protected IStatus run(IProgressMonitor monitor) {
			    	
			    	try {
						workspace.run(perform, monitor);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //move o m�todo para calcular m�tricas
					
			    	
			    	return Status.OK_STATUS;
			    }
				};

				// Start the Job
				job.schedule();

				job.join();*/
				
			
			workspace.run(perform, new NullProgressMonitor()); //move o m�todo para calcular m�tricas
			
			Thread.sleep(1000);
			
			/*boolean auxbol = false;
			do{
				for(int ii=0; ii < flags.length; ii++){
					if(flags[ii] == false)
						break;
					auxbol = true;
				}
			}while(!auxbol);
			
			
			for(int ii=0; ii < flags.length; ii++){
				flags[ii] = false;
			}
			*/
			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
			
			double[] metricsModified = QMoodMetrics.getMetrics(ms); //calcula as metricas apos mover m�todo

			
			Change undoChange = perform.getUndoChange();
			undoChange.perform(new NullProgressMonitor());
			
			/*Job job2 = new Job("My Job2") {
			    
			    @Override
			    protected IStatus run(IProgressMonitor monitor) {
			    	
			    	try {
						undoChange.perform(new NullProgressMonitor());
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //move o m�todo para a classe original
					
			    	
			    	return Status.OK_STATUS;
			    }
				};

				// Start the Job
				job2.schedule();

				job2.join();*/
			
			//Thread.sleep(1000);
			
			
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
	
	public boolean checkIfSomeMetricDecrease(double[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i] - metricsOriginal[i];
			if(aux < 0) {
				return true;
			}
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) < 0) return true;
		}
		
		return false;
	}
	
	public boolean checkIfSomeMetricIncrease(double[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i] - metricsOriginal[i];
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
	
	/*public void calculeMetrics(AbstractMetricSource ms){
		
		double[] metrics = QMoodMetrics.getMetrics(ms);
		
		System.out.print("REU: "+metrics[4]);
		System.out.print(" FLE: "+metrics[2]);
		System.out.print(" EFE: "+metrics[0]);
		System.out.print(" EXT: "+metrics[1]);
		System.out.print(" FUN: "+metrics[3]);
		System.out.println(" UND: "+metrics[5]);
		
	}*/
		
}
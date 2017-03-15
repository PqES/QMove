package qmove.movemethod;

import java.io.Serializable;


import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

import net.sourceforge.metrics.core.Metric;

@SuppressWarnings("restriction")
public class MethodsChosen implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ClassMethod method;
	private IVariableBinding targetChosen;
	private Metric[] metrics;
	

	public MethodsChosen(ClassMethod method, IVariableBinding targetChosen, Metric[] metrics){
		
		this.method = method;
		this.targetChosen = targetChosen;
		this.metrics = metrics;
		
	}
	
	public IMethod getMethod() {
		return method.getMethod();
	}

	public IVariableBinding getTargetChosen() {
		return targetChosen;
	}
	
	public String getpackageOriginal(){
		return method.getPackageName();
	}
	
	public String getClassOriginal(){
		return method.getClassName();
	}
	
	public Metric[] getMetrics() {
		return metrics;
	}
	
	public double calculePercentage(Metric[] metricsOriginal){
		double mediaMetricsOriginal = (metricsOriginal[0].getValue()+
										metricsOriginal[1].getValue()+
										metricsOriginal[2].getValue()+
										metricsOriginal[3].getValue()+
										metricsOriginal[4].getValue()+
										metricsOriginal[5].getValue())/6;
		
		double mediaNewMetrics = (metrics[0].getValue()+
									metrics[1].getValue()+
									metrics[2].getValue()+
									metrics[3].getValue()+
									metrics[4].getValue()+
									metrics[5].getValue())/6;
		
		double percentageIncrease = ((mediaNewMetrics - mediaMetricsOriginal) / mediaMetricsOriginal)*100;
		
		return percentageIncrease;
		
	}
	
	public void move() throws OperationCanceledException, CoreException{
	        
		
			MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method.getMethod(),
					JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));

			processor2.checkInitialConditions(new NullProgressMonitor());
			
			IVariableBinding[] potential = processor2.getPossibleTargets();
			
			IVariableBinding candidate = null;
			
			for(int j=0; j<potential.length; j++){
				if(targetChosen.toString().compareTo(potential[j].toString()) == 0){
					candidate = potential[j];
					break;
				}
			}
			
			processor2.setTarget(candidate);
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
		}
	
	@Override
    public MethodsChosen clone() throws CloneNotSupportedException {
        return (MethodsChosen) super.clone();
    }
	
}



package qmove.movemethod;


import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
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
import qmove.utils.SingletonNullProgressMonitor;

@SuppressWarnings("restriction")
public class MethodsChosen implements Cloneable{
	

	private ClassMethod method;
	private IVariableBinding targetChosen;
	private double[] metrics;
	

	public MethodsChosen(ClassMethod method, IVariableBinding targetChosen, double[] metrics){
		
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
	
	public IPackageFragment getpackageOriginal(){
		return method.getPackageName();
	}
	
	public IType getClassOriginal(){
		return method.getClassName();
	}
	
	public double[] getMetrics() {
		return metrics;
	}
	
	public double calculePercentage(double[] metricsOriginal){
		double mediaMetricsOriginal = (metricsOriginal[0]+
										metricsOriginal[1]+
										metricsOriginal[2]+
										metricsOriginal[3]+
										metricsOriginal[4]+
										metricsOriginal[5])/6;
		
		double mediaNewMetrics = (metrics[0]+
									metrics[1]+
									metrics[2]+
									metrics[3]+
									metrics[4]+
									metrics[5])/6;
		
		double percentageIncrease = ((mediaNewMetrics - mediaMetricsOriginal) / mediaMetricsOriginal)*100;
		
		return percentageIncrease;
		
	}
	
	public double getSumMetrics(){
		//Calibracao 4
		double sumMetrics = metrics[1]+metrics[3]+metrics[5];
		return sumMetrics;
		
	}
	
	public void move() throws OperationCanceledException, CoreException{
	        
		try{
			MoveInstanceMethodProcessor processor2 = null;
			while(processor2 == null){
					
					processor2 = new MoveInstanceMethodProcessor(method.getMethod(),
					JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));
			}
			
			processor2.checkInitialConditions(new NullProgressMonitor());
			
			//IVariableBinding[] potential = processor2.getPossibleTargets();
			
			IVariableBinding candidate = targetChosen;
			
			/*for(int j=0; j<potential.length; j++){
				if(targetChosen.toString().compareTo(potential[j].toString()) == 0 || targetChosen.equals(potential[j])){
					candidate = potential[j];
					break;
				}
			}*/
			
			processor2.setTarget(candidate);
			processor2.setInlineDelegator(true);
			processor2.setRemoveDelegator(true);
			processor2.setDeprecateDelegates(false);
	
			Refactoring refactoring2 = new MoveRefactoring(processor2);
			refactoring2.checkInitialConditions(new NullProgressMonitor());
			
			/*RefactoringStatus status2 = refactoring2.checkAllConditions(new NullProgressMonitor());
			if (status2.getSeverity() != RefactoringStatus.OK) return;
			 */
			
			final CreateChangeOperation create2 = new CreateChangeOperation(
						new CheckConditionsOperation(refactoring2,
						CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);
			
			PerformChangeOperation perform2 = new PerformChangeOperation(create2);
			
			ResourcesPlugin.getWorkspace().run(perform2, new NullProgressMonitor());
			
			/*IWorkspace workspace2 = ResourcesPlugin.getWorkspace();
			workspace2.run(perform2, new NullProgressMonitor());*/
		
		} catch(NullPointerException n){
			System.out.println(n.getMessage());
		}
	}
		
	
	
	
	@Override
    public MethodsChosen clone() throws CloneNotSupportedException {
        return (MethodsChosen) super.clone();
    }
	
}



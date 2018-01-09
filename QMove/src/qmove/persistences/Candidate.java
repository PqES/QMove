package qmove.persistences;


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

@SuppressWarnings("restriction")
public class Candidate implements Cloneable{
	
	private MethodTargets methodTargets;
	private IVariableBinding targetChosen;
	private double[] metrics;
	

	public Candidate(MethodTargets method, IVariableBinding targetChosen, double[] metrics){
		
		this.methodTargets = method;
		this.targetChosen = targetChosen;
		this.metrics = metrics;
		
	}
	
	public MethodTargets getMethodDeclaration() {
		return methodTargets;
	}
	
	public IMethod getMethod() {
		return methodTargets.getMethod();
	}

	public IVariableBinding getTargetChosen() {
		return targetChosen;
	}
	
	public IPackageFragment getpackageOriginal(){
		return getMethod().getDeclaringType().getPackageFragment();
	}
	
	public IType getClassOriginal(){
		return getMethod().getDeclaringType();
	}
	
	public double[] getMetrics() {
		return metrics;
	}
	
	public double calculePercentage(double[] metricsOriginal){
		double sumMetricsOriginal = (metricsOriginal[0]+
										metricsOriginal[1]+
										metricsOriginal[2]+
										metricsOriginal[3]+
										metricsOriginal[4]+
										metricsOriginal[5]);
		
		double sumNewMetrics = (metrics[0]+
									metrics[1]+
									metrics[2]+
									metrics[3]+
									metrics[4]+
									metrics[5]);
		
		double percentageIncrease = ((sumNewMetrics - sumMetricsOriginal) / Math.abs(sumMetricsOriginal))*100;
		
		return percentageIncrease;
		
	}
	
	public double getSumMetrics(double[] metricsOriginal){
		//Calibracao Relativa 3
		double sumMetricsOriginal = metricsOriginal[0]+metricsOriginal[1]+metricsOriginal[2]+metricsOriginal[3]+metricsOriginal[4]+metricsOriginal[5];
		double sumMetrics = metrics[0]+metrics[1]+metrics[2]+metrics[3]+metrics[4]+metrics[5];
		return ((sumMetrics-sumMetricsOriginal)/Math.abs(sumMetricsOriginal))*100;
		
	}
	
	public void move() throws OperationCanceledException, CoreException{
	        
		try{
			MoveInstanceMethodProcessor processor2 = null;
			while(processor2 == null){
					
					processor2 = new MoveInstanceMethodProcessor(getMethod(),
					JavaPreferencesSettings.getCodeGenerationSettings(getMethod().getJavaProject()));
			}
			
			processor2.checkInitialConditions(new NullProgressMonitor());
			
			processor2.setTarget(targetChosen);
			processor2.setInlineDelegator(true);
			processor2.setRemoveDelegator(true);
			processor2.setDeprecateDelegates(false);
	
			Refactoring refactoring2 = new MoveRefactoring(processor2);
			refactoring2.checkInitialConditions(new NullProgressMonitor());
			
			
			final CreateChangeOperation create2 = new CreateChangeOperation(
						new CheckConditionsOperation(refactoring2,
						CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);
			
			PerformChangeOperation perform2 = new PerformChangeOperation(create2);
			
			ResourcesPlugin.getWorkspace().run(perform2, new NullProgressMonitor());
			
			
		} catch(NullPointerException n){
			System.out.println(n.getMessage());
		}
	}
		
	
	
	
	@Override
    public Candidate clone() throws CloneNotSupportedException {
        return (Candidate) super.clone();
    }
	
}
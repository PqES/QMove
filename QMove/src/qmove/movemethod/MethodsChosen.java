package qmove.movemethod;

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

@SuppressWarnings("restriction")
public class MethodsChosen {
	
	private ClassMethod method;
	private IVariableBinding targetChosen;
	
	public MethodsChosen(ClassMethod method, IVariableBinding targetChosen){
		
		this.method = method;
		this.targetChosen = targetChosen;
		
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
}



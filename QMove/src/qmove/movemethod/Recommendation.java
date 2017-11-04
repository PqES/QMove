package qmove.movemethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.text.Position;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

import net.sourceforge.metrics.core.Metric;

public class Recommendation{
	
	private int qmoveID;
	private IMethod method;
	private IVariableBinding targetChosen;
	private double[] metrics;
	private double increase;
	private MethodsTable methodsTable;
	private IMethod methodOriginal;
	//MethodDeclaration md;
	

	public Recommendation(int qmoveID, MethodsTable methodsTable, MethodsChosen method, double increase, IMethod methodOriginal/*, MethodDeclaration md*/){
		this.qmoveID = qmoveID;
		this.methodsTable = methodsTable;
		this.method = method.getMethod();
		this.targetChosen = method.getTargetChosen();
		this.metrics = method.getMetrics();
		this.increase = increase;
		this.methodOriginal = methodOriginal;
		//this.md = md;
	}
	
	public IMethod getMethodOriginal() {
		return methodOriginal;
	}
	
	public int getQMoveID(){
		return qmoveID;
	}
	
	public IMethod getMethod() {
		return method;
	}

	public String getPackageMethodName() {
		return method.getCompilationUnit().getParent().getElementName();
	}

	public String getClassMethodName(){
		return method.getDeclaringType().getElementName();
	}

	public String getMethodName() {
		return method.getElementName();
	}

	public String getPackageTargetName() {
		return targetChosen.getType().getPackage().getName();
	}
	
	public IVariableBinding getTarget(){
		return targetChosen;
	}

	public String getClassTargetName() {
		return  targetChosen.getType().getName();
	}

	public double getIncrease() {
		return increase;
	}
	
	public MethodsTable getMethodsTable(){
		return methodsTable;
	}
	
	public IFile getSourceIFile() {
		// TODO Auto-generated method stub
		return (IFile) method.getCompilationUnit().getResource();
	}
	
	public IFile getTargetIFile() {
		// TODO Auto-generated method stub
		return (IFile) targetChosen.getJavaElement().getResource();
	}
	
	public List<Position> getPositions() {

		MethodDeclaration md = MethodObjects.getInstance().getMethodDeclaration(methodOriginal);
		//MethodDeclaration md = getMethodDeclaration();
		ArrayList<Position> positions = new ArrayList<Position>();
		Position position = new Position(md.getStartPosition(), md.getLength());
		positions.add(position);
		return positions;
	}
	
	public MethodDeclaration getMethodDeclaration() {
		
		/*String methodName = methodOriginal.getElementName();
		String className = methodOriginal.getCompilationUnit().getParent().getElementName() + "." + methodOriginal.getDeclaringType().getElementName();
		String methodMap;
		String classMap;
		
		for (Map.Entry<IMethod, MethodDeclaration> entrada : qmove.compilation.DependencyVisitor.mdMethods.entrySet()){
			
			methodMap = entrada.getKey().getElementName();
			classMap = entrada.getKey().getCompilationUnit().getParent().getElementName() + "." + methodOriginal.getDeclaringType().getElementName();
			
			if(methodName.compareTo(methodMap) == 0 && className.compareTo(classMap) == 0)
				return entrada.getValue();
		}*/
		
		return null;
	}
	
	/*public MethodDeclaration getSourceMethodDeclaration() {
		// TODO Auto-generated method stub
		return MethodObjects.getInstance().getMethodDeclaration(methodOriginal);
	}*/
	
	public String getAnnotationText() {
		Map<String, ArrayList<String>> accessMap = new LinkedHashMap<String, ArrayList<String>>();


		StringBuilder sb = new StringBuilder();
		Set<String> keySet = accessMap.keySet();
		int i = 0;
		for (String key : keySet) {
			ArrayList<String> entities = accessMap.get(key);
			sb.append(key + ": " + entities.size());
			if (i < keySet.size() - 1)
				sb.append(" | ");
			i++;
		}
		return sb.toString();
	}
	
	public void setMetrics(double[] metrics){
		this.metrics = metrics;
	}
	
	public void decreaseQMoveID(){
		qmoveID--;
	}
	
	/*public void move() throws OperationCanceledException, CoreException{
        
		
		MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
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
	}*/
	
	
}

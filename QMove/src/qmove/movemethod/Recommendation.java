package qmove.movemethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Position;

public class Recommendation{
	
	private int qmoveID;
	private MethodsChosen method;
	private double increase;
	private MethodsTable methodsTable;
	private IMethod methodOriginal;
	//MethodDeclaration md;
	

	public Recommendation(int qmoveID, MethodsTable methodsTable, MethodsChosen method, double increase, IMethod methodOriginal/*, MethodDeclaration md*/){
		this.qmoveID = qmoveID;
		this.methodsTable = methodsTable;
		this.method = method; 
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

	public String getPackageMethodName() {
		return method.getpackageOriginal().getElementName();
	}

	public String getClassMethodName(){
		return method.getClassOriginal().getElementName();
	}

	public String getMethodName() {
		return method.getMethod().getElementName();
	}

	public String getPackageTargetName() {
		return method.getTargetChosen().getType().getPackage().getName();
	}
	
	public IVariableBinding getTarget(){
		return method.getTargetChosen();
	}

	public String getClassTargetName() {
		return  method.getTargetChosen().getName();
	}

	public double getIncrease() {
		return increase;
	}
	
	public MethodsTable getMethodsTable(){
		return methodsTable;
	}
	
	public IFile getSourceIFile() {
		// TODO Auto-generated method stub
		return (IFile) method.getMethod().getCompilationUnit().getResource();
	}
	
	public IFile getTargetIFile() {
		// TODO Auto-generated method stub
		return (IFile) method.getTargetChosen().getJavaElement().getResource();
	}
	
	public List<Position> getPositions() {

		//MethodDeclaration md = MethodObjects.getInstance().getMethodDeclaration(methodOriginal);
		MethodDeclaration md = getMethodDeclaration();
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
	
	
}

package qmove.movemethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Position;

public class Recommendation {

	private int qmoveID;
	private String method;
	private String targetChosen;
	private double[] metrics;
	private double increase;
	private MethodsTable methodsTable;

	public Recommendation(int qmoveID, MethodsTable methodsTable, String method, String targetChosen, double[] metrics,double increase) {
		this.qmoveID = qmoveID;
		this.methodsTable = methodsTable;
		this.method = method;
		this.targetChosen = targetChosen;
		this.metrics = metrics;
		this.increase = increase;
	}

	public MethodDeclaration getMethodDeclaration(ArrayList<MethodDeclaration> allMethods) {
		for (int i = 0; i < allMethods.size(); i++) {
			IMethod m = (IMethod) allMethods.get(i).resolveBinding().getJavaElement();
			if (method.compareTo(m.getDeclaringType().getFullyQualifiedName() + "::" + m.getElementName()) == 0) {
				return allMethods.get(i);
			}
		}

		return null;
	}

	public IMethod getIMethod(ArrayList<MethodDeclaration> allMethods, IJavaProject project, IProject p) {
		return (IMethod) getMethodDeclaration(allMethods).resolveBinding().getJavaElement();
	}

	public int getQMoveID() {
		return qmoveID;
	}

	public String getMethod() {
		return method;
	}

	public String getTarget() {
		return targetChosen;
	}

	public double getIncrease() {
		return increase;
	}

	public MethodsTable getMethodsTable() {
		return methodsTable;
	}

	public double[] getMetrics() {
		return metrics;
	}

	/*
	 * public IFile getSourceIFile() { return (IFile)
	 * getIMethod().getCompilationUnit().getResource(); }
	 * 
	 * public IFile getTargetIFile() { return (IFile)
	 * targetChosen.getJavaElement().getResource(); }
	 */

	public List<Position> getPositions(MethodDeclaration md) {

		ArrayList<Position> positions = new ArrayList<Position>();
		Position position = new Position(md.getStartPosition(), md.getLength());
		positions.add(position);
		return positions;
	}

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

	public void setMetrics(double[] metrics) {
		this.metrics = metrics;
	}

	public void decreaseQMoveID() {
		qmoveID--;
	}

}

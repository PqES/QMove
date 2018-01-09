package qmove.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import qmove.ast.MethodsVisitor;
import qmove.movemethod.MoveMethod;
import qmove.persistences.Candidate;
import qmove.persistences.MethodTargets;
import qmove.persistences.MethodsTable;
import qmove.persistences.Recommendation;
import qmove.utils.CSVUtils;
import qmove.utils.MetricsUtils;
import qmove.utils.SingletonNullProgressMonitor;

@SuppressWarnings("restriction")
public class MoveMethodHandler extends AbstractHandler {

	public static ArrayList<Recommendation> listRecommendations = new ArrayList<Recommendation>();
	public static IProject iProject = null;
	private ArrayList<MethodTargets> potRefactor;
	private Map<String, double[]> methodsTable = new HashMap<String, double[]>();
	private IJavaProject projectCopy;
	private double[] metricsOriginal;
	private AbstractMetricSource ms;
	private ArrayList<MethodDeclaration> allMethods;
	private Candidate bestMethod;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

			hideViewIfOpen();

			iProject = getProjectFromWorkspace(event);

			System.out.print("Clonando projeto " + iProject.getName() + "...");
			projectCopy = cloneProject();
			System.out.println("OK");

			System.out.print("Buscando por todos os metodos do projeto...");
			allMethods = getAllMethods(projectCopy.getProject());
			System.out.println("OK");

			System.out.print("Calculando metricas do estado atual de " + iProject.getName() + "... ");

			MetricsUtils.waitForMetricsCalculate();

			System.out.println("Pronto!");

			System.out.println("Valores das metricas atuais:");
			ms = Dispatcher.getAbstractMetricSource(projectCopy.getPrimaryElement());
			metricsOriginal = MetricsUtils.getMetrics(ms);

			CSVUtils.initializeCsvFile();

			MoveMethod mm = new MoveMethod(metricsOriginal);
			potRefactor = new ArrayList<MethodTargets>();

			System.out.println("Verificando metodos que podem ser movidos: ");
			for (Iterator<MethodDeclaration> i = allMethods.iterator(); i.hasNext();) {
				MethodDeclaration md = i.next();
				if (mm.canMethodBeMoved((IMethod) md.resolveBinding().getJavaElement())) {
					potRefactor.add(mm.getMethodTargets());
				}
				i.remove();
			}

			mm.setMethodTargets(null);
			Candidate aux;
			int qmoveID = 0;

			System.out.println("Quantidade de metodos que podem ser movidos: "+potRefactor.size());

			while (potRefactor.size() > 0) {

				CSVUtils.writeCsvFile("Current", metricsOriginal);

				for (int i = 0; i < potRefactor.size(); i++) {
					System.out.println("Metodo " + (i + 1) + " de " + potRefactor.size());
					aux = mm.doRefactoringProcess(potRefactor.get(i));
					if (aux != null){
						if(bestMethod==null){
							bestMethod = aux;
							methodsTable.put(bestMethod.getMethod().getElementName()+", "+bestMethod.getTargetChosen().getType().getName(), bestMethod.getMetrics());
						} else {
							if(aux.calculePercentage(metricsOriginal) > bestMethod.calculePercentage(metricsOriginal)){
								bestMethod = aux;
								methodsTable.put(bestMethod.getMethod().getElementName()+", "+bestMethod.getTargetChosen().getType().getName(), bestMethod.getMetrics());
							}
						}
					}
				}

				if (bestMethod == null) {
					potRefactor.clear();
					continue;
				}
				

				
				
				String methodName = bestMethod.getMethod().getDeclaringType().getFullyQualifiedName()+"::"+ bestMethod.getMethod().getElementName();
				String targetName = bestMethod.getTargetChosen().getType().getPackage().getName()+"."+bestMethod.getTargetChosen().getType().getName();
				
				CSVUtils.writeRecInFile(++qmoveID,methodName,targetName,bestMethod.calculePercentage(metricsOriginal));
				
				listRecommendations.add(new Recommendation(qmoveID,
														   new MethodsTable(metricsOriginal, methodsTable),
														   methodName, 
														   targetName, 
														   bestMethod.getMetrics(), 
														   bestMethod.calculePercentage(metricsOriginal)));
				
				bestMethod.move();
				
				metricsOriginal = bestMethod.getMetrics();
				mm.setMetricsOriginal(metricsOriginal);

				potRefactor.remove(bestMethod.getMethodDeclaration());

				bestMethod = null;

			}

			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("qmove.views.QMoveView");

			projectCopy.getProject().delete(true, SingletonNullProgressMonitor.getNullProgressMonitor());

		} catch (CoreException e) {
			e.printStackTrace();
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void hideViewIfOpen() {
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// Find desired view :
		IViewPart myView = wp.findView("qmove.views.QMoveView");

		// Hide the view :
		wp.hideView(myView);

	}

	
	private IProject getProjectFromWorkspace(ExecutionEvent event) {

		TreeSelection selection = (TreeSelection) HandlerUtil.getCurrentSelection(event);

		if (selection == null || selection.getFirstElement() == null) {
			// Nothing selected, do nothing
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information", "Please select a project");
			return null;
		}

		JavaProject jp;
		Project p;

		try {
			jp = (JavaProject) selection.getFirstElement();
			return jp.getProject();
		} catch (ClassCastException e) {
			p = (Project) selection.getFirstElement();
			return p.getProject();
		}
	}

	private IJavaProject cloneProject() throws CoreException {
		IProgressMonitor m = new NullProgressMonitor();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProjectDescription projectDescription = iProject.getDescription();
		String cloneName = iProject.getName() + "Temp";
		// create clone project in workspace
		IProjectDescription cloneDescription = workspaceRoot.getWorkspace().newProjectDescription(cloneName);
		// copy project files
		iProject.copy(cloneDescription, true, m);
		IProject clone = workspaceRoot.getProject(cloneName);

		cloneDescription.setNatureIds(projectDescription.getNatureIds());
		cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
		cloneDescription.setDynamicReferences(projectDescription.getDynamicReferences());
		cloneDescription.setBuildSpec(projectDescription.getBuildSpec());
		cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
		clone.setDescription(cloneDescription, null);
		clone.open(m);
		return JavaCore.create(clone);
	}

	private ArrayList<MethodDeclaration> getAllMethods(IProject project) {

		try {

			ArrayList<MethodDeclaration> allMethods = new ArrayList<MethodDeclaration>();

			project.accept(new IResourceVisitor() {

				@Override
				public boolean visit(IResource resource) throws JavaModelException {
					if (resource instanceof IFile && resource.getName().endsWith(".java")) {
						ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
						MethodsVisitor mv = new MethodsVisitor(unit);
						if(mv.getMethods() != null){
							allMethods.addAll(mv.getMethods());
						}

					}
					return true;
				}
			});

			return allMethods;

		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}

	}

}

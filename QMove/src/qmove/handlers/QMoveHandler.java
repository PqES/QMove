package qmove.handlers;

import java.util.ArrayList;

import javax.swing.JOptionPane;

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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import qmove.ast.ClassMethodVisitor;
import qmove.checker.MethodMetricsChecker;
import qmove.metrics.qmood.calculator.QMOOD;
import qmove.persistence.MethodTargets;
import qmove.persistence.Recommendation;
import qmove.persistence.ValidMove;
import qmove.utils.FileUtils;
import qmove.utils.MoveMethodUtils;
import qmove.utils.SingletonNullProgressMonitor;
import qmove.utils.ViewUtils;

@SuppressWarnings("restriction")
public class QMoveHandler extends AbstractHandler {

	private ArrayList<IType> allTypes;
	private ArrayList<IMethod> allMethods;
	private ArrayList<MethodTargets> allPossibleRefactorings;
	public static ArrayList<Recommendation> recommendations;
	public static IJavaProject projectOriginal;
	public static IJavaProject projectCopy;
	public static String calibrationType;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			
			allTypes = new ArrayList<IType>();
			allMethods = new ArrayList<IMethod>();
			allPossibleRefactorings = new ArrayList<MethodTargets>();
						

			// hide view if is open
			ViewUtils.hideView();

			// get selected project from project/package explorer
			projectOriginal = getProjectFromWorkspace(event);
			
			// if null, throw a NullPointerException and stop execution
			projectOriginal.exists();
			
			// choosing a calibration type
			calibrationType = showCalibrationDialog();
			
			// if null, throw a NullPointerException and stop execution
			calibrationType.isEmpty();

			// clone project
			System.out.print("Cloning project... ");
			projectCopy = cloneProject(projectOriginal.getProject());
			System.out.println("OK!");

			// get all classes and methods from project
			System.out.print("Getting project classes and methods...... ");
			getAllClassesAndMethods(projectCopy);
			System.out.println("OK!");

			// calculate all current metrics
			System.out.print("Calculating current metrics... ");
			QMOOD qmood = new QMOOD(allTypes);
			System.out.println("OK!");

			// get all methods that can be moved
			MethodMetricsChecker mmc = new MethodMetricsChecker(qmood);
			MethodTargets mtAux;
			for (IMethod method : allMethods) {
				mtAux = MoveMethodUtils.canMoveMethod(method);
				if (mtAux != null) {
					allPossibleRefactorings.add(mtAux);
				}
			}

			// turn objects visible to garbage collector if possible
			allMethods = null;
			mtAux = null;

			// variable for the recommendations id
			int count = 0, countID;

			// find best move method refactoring sequence
			while (!allPossibleRefactorings.isEmpty()) {

				System.out.println("Current QMOOD metrics");
				printMetrics(qmood);

				ValidMove bestRefactoring = null, actualRefactoring;

				// set current metrics
				mmc.setMetrics(qmood.getQMOODAttributes());
				
				countID=0;
				for (MethodTargets mt : allPossibleRefactorings) {
					countID++;
					System.out.println("---------------------------------------------------");
					System.out.println("Method "+countID+" of "+allPossibleRefactorings.size());
					actualRefactoring = mmc.refactorAndCalculateMetrics(mt);
					if (actualRefactoring != null) {

						FileUtils.writeBetterMethod(
								actualRefactoring.getMethod().getDeclaringType().getFullyQualifiedName() + "::"
										+ actualRefactoring.getMethod().getElementName(),
								actualRefactoring.getTarget(), actualRefactoring.getIncrease(),
								actualRefactoring.getNewMetrics());

						if (bestRefactoring == null) {
							bestRefactoring = actualRefactoring;
						} else if (actualRefactoring.getIncrease() > bestRefactoring.getIncrease()) {
							bestRefactoring = actualRefactoring;
						}
					}
				}

				if (bestRefactoring == null) {
					allPossibleRefactorings.clear();
				} else {

					FileUtils.writeRecommendation(++count,
							bestRefactoring.getMethod().getDeclaringType().getFullyQualifiedName() + "::"
									+ bestRefactoring.getMethod().getElementName(),
							bestRefactoring.getTarget(), bestRefactoring.getOldMetrics(),
							bestRefactoring.getNewMetrics(), bestRefactoring.getIncrease(),
							bestRefactoring.getMethod().getParameterTypes());

					MoveMethodUtils.moveBestMethod(bestRefactoring.getMethod(), bestRefactoring.getTarget());
					qmood.recalculateMetrics(bestRefactoring.getTypes());
					IMethod bestMethod = bestRefactoring.getMethod();
					allPossibleRefactorings.removeIf(filter -> filter.getMethod().equals(bestMethod));
				}

			}

			// delete project copy
			projectCopy.getProject().delete(true, SingletonNullProgressMonitor.getNullProgressMonitor());

			// get recommendations from file
			recommendations = FileUtils.readRecommendationsFile();

			// open view
			ViewUtils.openView();

		} catch (CoreException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			return null;
		}

		return null;
	}

	private void printMetrics(QMOOD qmood) {
		System.out.print("EFE = " + qmood.getEfe());
		System.out.print(" EXT = " + qmood.getExt());
		System.out.print(" FLE = " + qmood.getFle());
		System.out.print(" FUN = " + qmood.getFun());
		System.out.print(" REU = " + qmood.getReu());
		System.out.println(" UND = " + qmood.getUnd());
	}

	private void getAllClassesAndMethods(final IJavaProject jprojectCopy) throws CoreException {

		try {
			jprojectCopy.getProject().accept(new IResourceVisitor() {

				@Override
				public boolean visit(IResource resource) throws JavaModelException {
					if (resource instanceof IFile && resource.getName().endsWith(".java")) {
						ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));

						ClassMethodVisitor cmv = new ClassMethodVisitor(unit);
						if (cmv.getArrayTypes() != null) {
							allTypes.addAll(cmv.getArrayTypes());
						}

						if (cmv.getArrayMethod() != null) {
							allMethods.addAll(cmv.getArrayMethod());
						}

					}
					return true;
				}
			});

		}

		// another way to read class and methods if the first one don't work
		catch (NullPointerException e) {
			IPackageFragment[] packages = jprojectCopy.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
						IType[] types = unit.getTypes();
						for (int i = 0; i < types.length; i++) {
							IType type = types[i];
							allTypes.add(type);
							IMethod[] imethods = type.getMethods();
							for (int j = 0; j < imethods.length; j++) {
								if (!imethods[j].getDeclaringType().isAnonymous()) {
									allMethods.add(imethods[j]);
								}
							}
						}
					}
				}
			}
		}
	}

	private IJavaProject getProjectFromWorkspace(ExecutionEvent event) {

		TreeSelection selection = (TreeSelection) HandlerUtil.getCurrentSelection(event);

		if (selection == null || selection.getFirstElement() == null) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information", "Please select a project");
			return null;
		}

		JavaProject jp;
		Project p;

		try {
			jp = (JavaProject) selection.getFirstElement();
			return JavaCore.create(jp.getProject());
		} catch (ClassCastException e) {
			p = (Project) selection.getFirstElement();
			return JavaCore.create(p.getProject());
		}
	}

	@SuppressWarnings("deprecation")
	private IJavaProject cloneProject(IProject iProject) throws CoreException {

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProjectDescription projectDescription = iProject.getDescription();
		String cloneName = iProject.getName() + "Temp";

		// create clone project in workspace
		IProjectDescription cloneDescription = workspaceRoot.getWorkspace().newProjectDescription(cloneName);

		// copy project files
		iProject.copy(cloneDescription, true, SingletonNullProgressMonitor.getNullProgressMonitor());
		IProject clone = workspaceRoot.getProject(cloneName);

		cloneDescription.setNatureIds(projectDescription.getNatureIds());
		cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());
		cloneDescription.setDynamicReferences(projectDescription.getDynamicReferences());
		cloneDescription.setBuildSpec(projectDescription.getBuildSpec());
		cloneDescription.setReferencedProjects(projectDescription.getReferencedProjects());

		clone.setDescription(cloneDescription, null);
		clone.open(IResource.BACKGROUND_REFRESH, SingletonNullProgressMonitor.getNullProgressMonitor());

		return JavaCore.create(clone);
	}
	
	private String showCalibrationDialog(){
		
		Object[] possibilities = {"Abs#1", "Rel#1", "Abs#2", "Rel#2", "Abs#3", "Rel#3", "Abs#4", "Rel#4", "Abs#5", "Rel#5"};
		
		
		String s = (String)JOptionPane.showInputDialog(
							null,
		                    "Choose the calibration type you prefer:\n",		                    
		                    "Choose a Calibration",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    possibilities,
		                    possibilities[5]);
		return s;
	}
}

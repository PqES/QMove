package qmove.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import qmove.moveclass.MCRecommendation;
import qmove.movemethod.MethodsChosen;
import qmove.movemethod.QMoodMetrics;

public class HandlerUtils {

	public static boolean queueIsZero;

	public static IJavaProject getProjectFromWorkspace(ExecutionEvent event) {

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
			return JavaCore.create(jp.getProject());
		} catch (ClassCastException e) {
			p = (Project) selection.getFirstElement();
			return JavaCore.create(p.getProject());
		}
	}

	public static ArrayList<IType> getAllClasses(IProject project) {

		try {
			ArrayList<IType> allClasses = new ArrayList<IType>();
			IJavaProject projectTemp = JavaCore.create(project);
			IPackageFragment[] packages;

			packages = projectTemp.getPackageFragments();

			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
						IType[] types = unit.getTypes();
						for (IType type : types) {
							allClasses.add(type);
						}

					}
				}
			}

			return allClasses;

		} catch (JavaModelException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IJavaProject cloneProject(IProject iProject) {
		try {
			IProgressMonitor m = new NullProgressMonitor();
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProjectDescription projectDescription;

			projectDescription = iProject.getDescription();

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

		} catch (CoreException e) {

			e.printStackTrace();
			return null;
		}
	}

	public static double[] calculateMetrics(IJavaElement je) {
		try {

			queueIsZero = false;

			while (queueIsZero == false) {
				Thread.sleep(100);
			}

			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
			return QMoodMetrics.getMetrics(ms);

		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static double compareMetrics(double[] currentMetrics, double[] newMetrics) {
		// Calibracao Relativa 3
		double sumOriginal = 0, sumModified = 0;

		for (int i = 0; i <= 5; i++) {
			sumOriginal += currentMetrics[i];
			sumModified += newMetrics[i];
		}

		return ((sumModified - sumOriginal) / Math.abs(sumOriginal)) * 100;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static MCRecommendation maxMetrics(ArrayList<MCRecommendation> candidates) {
		
		if(candidates.size() == 1){
			return candidates.get(0);
		}
		
		Collections.sort(candidates, new Comparator() {
			public int compare(Object o1, Object o2) {
				MCRecommendation m1 = (MCRecommendation) o1;
				MCRecommendation m2 = (MCRecommendation) o2;
				return m1.getIncrease() > m2.getIncrease() ? -1 : (m1.getIncrease() < m2.getIncrease() ? +1 : 0);
			}
		});
		
		return candidates.get(0);
	}

}

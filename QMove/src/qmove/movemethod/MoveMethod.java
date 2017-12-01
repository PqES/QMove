package qmove.movemethod;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import net.sourceforge.metrics.ui.MetricsView;
import qmove.core.QMoveHandler;
import qmove.utils.SingletonNullProgressMonitor;
import qmove.utils.qMooveUtils;

@SuppressWarnings("restriction")
public class MoveMethod {

	private ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	private MethodMetric candidateChosen;
	private double[] metricsOriginal;
	private IJavaElement je;
	public static double[] metricsModified;

	public MoveMethod(IJavaElement je) {
		this.je = je;

	}

	public boolean ckeckIfMethodCanBeMoved(ClassMethod method) throws OperationCanceledException, CoreException {

		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method.getMethod(),
				JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());

		IVariableBinding[] potential = processor.getPossibleTargets();

		if (potential.length == 0 || potential == null)
			return false;

		else {

			for (int i = 0; i < potential.length; i++) {

				MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method.getMethod(),
						JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));

				processor2.setTarget(potential[i]);
				processor2.setInlineDelegator(true);
				processor2.setRemoveDelegator(true);
				processor2.setDeprecateDelegates(false);

				Refactoring refactoring = new MoveRefactoring(processor2);
				try{
					
				
				RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());

				if (!status.isOK())
					return false;

				else {
					Change undoChange = refactoring.createChange(new NullProgressMonitor());
					if (undoChange == null)
						return false;
				}

				processor2 = null;
				} catch(IllegalArgumentException e){
					return false;
				}
			}
		}

		return true;

	}

	public MethodsChosen startRefactoring(ClassMethod method, double[] metricsOriginal) {

		this.metricsOriginal = metricsOriginal;

		MethodsChosen methodMoved = null;

		try {
			if (canMove(method.getMethod()))
				methodMoved = new MethodsChosen(method, candidateChosen.getPotential(), candidateChosen.getMetrics());
		} catch (OperationCanceledException | CoreException | InterruptedException e) {
			e.printStackTrace();
		}

		return methodMoved;
	}

	public boolean canMove(IMethod method) throws OperationCanceledException, CoreException, InterruptedException {

		try {
			MoveInstanceMethodProcessor processor = null;

			while (processor == null) {
				processor = new MoveInstanceMethodProcessor(method,
						JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));
			}

			processor.checkInitialConditions(new NullProgressMonitor());

			IVariableBinding[] potential = processor.getPossibleTargets();

			if (potential.length == 0 || potential == null)
				return false;

			System.out.println("-----------Metodo: " + method.getElementName() + "----------------");

			IVariableBinding candidate;

			for (int i = 0; i < potential.length; i++) {
				
				if(potential[i].getType().isInterface()){
					continue;
				}

				MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
						JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

				processor2.checkInitialConditions(new NullProgressMonitor());

				candidate = potential[i];

				System.out.println("Calculando refactoring para "
						+ candidate.getType().getName());

				processor2.setTarget(candidate);
				processor2.setInlineDelegator(true);
				processor2.setRemoveDelegator(true);
				processor2.setDeprecateDelegates(false);

				Refactoring refactoring = new MoveRefactoring(processor2);
				refactoring.checkInitialConditions(new NullProgressMonitor());

				final CreateChangeOperation create = new CreateChangeOperation(
						new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);

				PerformChangeOperation perform = new PerformChangeOperation(create);

				ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());

				System.out.print("Recalculando metricas... ");

				QMoveHandler.queueIsZero = false;
				while (QMoveHandler.queueIsZero == false) {
					Thread.sleep(100);
				}

				System.out.println("Pronto!");

				AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
				metricsModified = QMoodMetrics.getMetrics(ms);
				qMooveUtils.writeCsvFile(
						method.getCompilationUnit().getParent().getElementName() + "."
								+ method.getDeclaringType().getElementName() + ":" + method.getElementName() + " / "
								+ candidate.getType().getPackage().getName() + "." + candidate.getName(),
						metricsModified);
				
				Change undoChange = perform.getUndoChange();
				
				undoChange.perform(SingletonNullProgressMonitor.getNullProgressMonitor());

				System.out.print("Recalculando metricas... ");

				QMoveHandler.queueIsZero = false;
				while (QMoveHandler.queueIsZero == false) {
					Thread.sleep(100);
				}

				System.out.println("Pronto!");


				if (hadMetricsIncreased()) {
					System.out.println("Melhoram metricas");
					potentialFiltred.add(new MethodMetric(potential[i], metricsModified));
					continue;
				} else{
					System.out.println("Pioram metricas");
				}


			}

		} catch (NullPointerException n) {
			System.out.println(n.getMessage());
		}

		if (choosePotential()) {
			potentialFiltred.clear();
			return true;
		}

		else
			return false;

	}

	private boolean hadMetricsIncreased() {
		
			//Calibracao Relativa 2
			for(int i=0; i <= 5; i++){
				if(i==2){
					continue;
				}
				if(((metricsModified[i] - metricsOriginal[i])/ Math.abs(metricsOriginal[i]))*100 < 0){
					return false;
				}
			}
			
			for(int i=0; i <= 5; i++){
				if(i==2){
					continue;
				}
				if(((metricsModified[i] - metricsOriginal[i])/ Math.abs(metricsOriginal[i]))*100 > 0){
					return true;
				}
			}
				
			return false;
	}

	public boolean choosePotential() throws OperationCanceledException, CoreException {
		
		if (potentialFiltred.size() == 0 || potentialFiltred == null)
			return false;

		if (potentialFiltred.size() == 1) {
			candidateChosen = potentialFiltred.get(0);
			return true;
		}

		else {

			candidateChosen = potentialFiltred.get(0);

			for (int i = 1; i < potentialFiltred.size(); i++) {

				if (potentialFiltred.get(i).hasBetterMetricsThan(metricsOriginal, candidateChosen.getMetrics()))
					candidateChosen = potentialFiltred.get(i);
			}

			return true;
		}

	}

}
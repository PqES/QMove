package qmove.movemethod;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import qmove.utils.SingletonNullProgressMonitor;
import qmove.utils.CSVUtils;
import qmove.utils.MetricsUtils;

@SuppressWarnings("restriction")
public class MoveMethod {

	private Map<IVariableBinding, double[]> potentials = new HashMap<IVariableBinding, double[]>();
	private double[] metricsOriginal;
	private double[] metricsModified;
	private AbstractMetricSource ms;
	private IVariableBinding targetChosen;
	private double[] newMetrics;

	public MoveMethod(double[] metricsOriginal) {
		this.metricsOriginal = metricsOriginal;

	}

	public boolean ckeckIfMethodCanBeMoved(IMethod method) throws OperationCanceledException, CoreException {

		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
				JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());

		IVariableBinding[] potential = processor.getPossibleTargets();

		if (potential.length == 0 || potential == null) {
			return false;
		}

		else {

			for (int i = 0; i < potential.length; i++) {

				MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
						JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

				processor2.setTarget(potential[i]);
				processor2.setInlineDelegator(true);
				processor2.setRemoveDelegator(true);
				processor2.setDeprecateDelegates(false);

				Refactoring refactoring = new MoveRefactoring(processor2);
				try {

					RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());

					if (!status.isOK()) {
						return false;
					}

					else {
						Change undoChange = refactoring.createChange(new NullProgressMonitor());
						if (undoChange == null) {
							return false;
						}
					}

					processor2 = null;
				} catch (IllegalArgumentException e) {
					return false;
				}
			}
		}

		return true;

	}

	public Candidate startRefactoring(MethodDeclaration method) {

		Candidate methodMoved = null;

		try {
			if (canMove((IMethod) method.resolveBinding().getJavaElement())) {
				methodMoved = new Candidate(method, targetChosen, newMetrics);
			}
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

				if (potential[i].getType().isInterface()) {
					continue;
				}

				MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
						JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

				processor2.checkInitialConditions(new NullProgressMonitor());

				candidate = potential[i];

				System.out.println("Calculando refactoring para " + candidate.getType().getName());

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

				MetricsUtils.waitForMetricsCalculate();

				System.out.println("Pronto!");

				ms = Dispatcher.getAbstractMetricSource(method.getJavaProject().getPrimaryElement());
				metricsModified = MetricsUtils.getMetrics(ms);

				CSVUtils.writeCsvFile(
						method.getCompilationUnit().getParent().getElementName() + "."
								+ method.getDeclaringType().getElementName() + ":" + method.getElementName() + " / "
								+ candidate.getType().getPackage().getName() + "." + candidate.getName(),
						metricsModified);

				Change undoChange = perform.getUndoChange();

				undoChange.perform(SingletonNullProgressMonitor.getNullProgressMonitor());

				System.out.print("Recalculando metricas... ");

				MetricsUtils.waitForMetricsCalculate();

				System.out.println("Pronto!");

				if (hadMetricsIncreased()) {
					System.out.println("Melhoram metricas");
					potentials.put(potential[i], metricsModified);
					continue;
				} else {
					System.out.println("Pioram metricas");
				}

			}

		} catch (NullPointerException n) {
			System.out.println(n.getMessage());
		}

		if (choosePotential()) {
			potentials.clear();
			return true;
		}

		else
			return false;

	}

	private boolean hadMetricsIncreased() {

		// Calibracao Relativa 3
		double sumOriginal = 0, sumModified = 0;

		for (int i = 0; i <= 5; i++) {
			sumOriginal += metricsOriginal[i];
			sumModified += metricsModified[i];
		}

		if (((sumModified - sumOriginal) / Math.abs(sumOriginal)) * 100 > 0) {
			return true;
		}

		return false;
	}

	public boolean choosePotential() {

		if (potentials.size() == 0 || potentials == null) {
			return false;
		}

		if (potentials.size() == 1) {
			targetChosen = (IVariableBinding) potentials.keySet().toArray()[0];
			newMetrics = potentials.get(targetChosen);
			return true;
		}

		else {

			targetChosen = (IVariableBinding) potentials.keySet().toArray()[0];

			for (int i = 1; i < potentials.keySet().toArray().length; i++) {

				if (hasBetterMetricsThan(potentials.get(targetChosen),
						potentials.get((IVariableBinding) potentials.keySet().toArray()[i]))) {
					targetChosen = (IVariableBinding) potentials.keySet().toArray()[i];
					newMetrics = potentials.get((IVariableBinding) potentials.keySet().toArray()[i]);
				}
			}

			return true;
		}

	}

	public boolean hasBetterMetricsThan(double[] currentBestMetrics, double[] candidateBestMetrics) {
		// Calibracao Relativa 3
		double sumMetricsOriginal = currentBestMetrics[0] + currentBestMetrics[1] + currentBestMetrics[2]
				+ currentBestMetrics[3] + currentBestMetrics[4] + currentBestMetrics[5];
		double sumMetrics = metricsOriginal[0] + metricsOriginal[1] + metricsOriginal[2] + metricsOriginal[3]
				+ metricsOriginal[4] + metricsOriginal[5];
		double sumCandidateMetrics = candidateBestMetrics[0] + candidateBestMetrics[1] + candidateBestMetrics[2]
				+ candidateBestMetrics[3] + candidateBestMetrics[4] + candidateBestMetrics[5];
		double percentageActual = ((sumMetrics - sumMetricsOriginal) / Math.abs(sumMetricsOriginal)) * 100;
		double percentageCandidate = ((sumCandidateMetrics - sumMetricsOriginal) / Math.abs(sumMetricsOriginal)) * 100;

		if (percentageActual > percentageCandidate) {
			return true;
		}

		return false;
	}

	public void setMetricsOriginal(double[] metricsOriginal) {
		this.metricsOriginal = metricsOriginal;
	}

}
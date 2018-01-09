package qmove.movemethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
import qmove.persistences.Candidate;
import qmove.persistences.MethodTargets;
import qmove.utils.CSVUtils;
import qmove.utils.MetricsUtils;
import qmove.utils.SingletonNullProgressMonitor;

@SuppressWarnings("restriction")
public class MoveMethod {

	private Map<IVariableBinding, double[]> potentials = new HashMap<IVariableBinding, double[]>();
	private double[] metricsOriginal;
	private double[] metricsModified;
	private AbstractMetricSource ms;
	private IVariableBinding targetChosen;
	private double[] newMetrics;
	private MethodTargets methodTargets;

	public MoveMethod(double[] metricsOriginal) {
		this.metricsOriginal = metricsOriginal;
		methodTargets = null;

	}

	public boolean canMethodBeMoved(IMethod method) throws OperationCanceledException, CoreException {

		boolean temUmValido = false;
		ArrayList<String> validTargets;

		try {
			System.out.println("---------------------------------------------------------------------");
			System.out.print("Tentando method " + method.getElementName() + "... ");
			if (method.isConstructor()) {
				System.out.println("Eh construtor!");
				return false;
			}

			MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			IVariableBinding[] targets = processor.getPossibleTargets();

			if (targets.length == 0 || targets == null) {
				System.out.println("Nao da pra mover pra nenhum lugar");
				return false;
			}

			else {

				validTargets = new ArrayList<String>();

				System.out.println();

				for (int i = 0; i < targets.length; i++) {

					IVariableBinding candidate = targets[i];
					System.out.print("Destino: " + candidate.getType().getName() + ": ");

					if (candidate.getType().isEnum() || candidate.getType().isInterface()) {
						System.out.println("É enumerado ou interface");
						continue;
					}

					processor = new MoveInstanceMethodProcessor(method,
							JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

					processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

					processor.setTarget(candidate);
					processor.setInlineDelegator(true);
					processor.setRemoveDelegator(true);
					processor.setDeprecateDelegates(false);

					Refactoring ref = new MoveRefactoring(processor);
					RefactoringStatus status = null;

					status = ref.checkAllConditions(new NullProgressMonitor());

					if (status.isOK()) {

						System.out.println("OK!");

						if (validTargets.contains(candidate.getType().getQualifiedName())) {
							System.out.println("Soh que esse destino ja ta salvo entre os destinos possiveis");
							continue;
						} else {
							validTargets.add(candidate.getType().getQualifiedName());
							temUmValido = true;
						}

					} else {
						System.out.println("Falhou");
					}

				}
			}

			if (temUmValido) {
				setMethodTargets(new MethodTargets(method, validTargets));
			}

			return temUmValido;

		} catch (Exception e) {
			return false;
		}
	}

	public Candidate doRefactoringProcess(MethodTargets methodTargets) {

		Candidate methodMoved = null;

		try {
			System.out.println("-----------Metodo: " + methodTargets.getMethod().getElementName() + "----------------");
			if (improvedMetrics(methodTargets)) {
				methodMoved = new Candidate(methodTargets, targetChosen, newMetrics);
			}
		} catch (OperationCanceledException | CoreException | InterruptedException e) {
			e.printStackTrace();
		}

		return methodMoved;
	}

	public boolean improvedMetrics(MethodTargets methodTargets)
			throws OperationCanceledException, CoreException, InterruptedException {

		try {
			MoveInstanceMethodProcessor processor = null;

			while (processor == null) {
				processor = new MoveInstanceMethodProcessor(methodTargets.getMethod(),
						JavaPreferencesSettings.getCodeGenerationSettings(methodTargets.getMethod().getJavaProject()));
			}

			processor.checkInitialConditions(new NullProgressMonitor());

			IVariableBinding[] targets = processor.getPossibleTargets();
			
			// Array que ira armazenar os targets ja encontrados para o metodo
			ArrayList<IVariableBinding> arrayTargets = new ArrayList<IVariableBinding>();


			// laco que popula a arrayTargets
			for (IVariableBinding target : targets) {
				for (String targetDetected : methodTargets.getTargets()) {
					if (targetDetected.compareTo(target.getType().getQualifiedName()) == 0) {
						arrayTargets.add(target);
					}
				}
			}

			for (IVariableBinding candidate : arrayTargets) {


				processor = new MoveInstanceMethodProcessor(methodTargets.getMethod(),
						JavaPreferencesSettings.getCodeGenerationSettings(methodTargets.getMethod().getJavaProject()));

				processor.checkInitialConditions(new NullProgressMonitor());


				System.out.println("Movendo para " + candidate.getType().getName());

				processor.setTarget(candidate);
				processor.setInlineDelegator(true);
				processor.setRemoveDelegator(true);
				processor.setDeprecateDelegates(false);

				Refactoring refactoring = new MoveRefactoring(processor);
				refactoring.checkInitialConditions(new NullProgressMonitor());

				final CreateChangeOperation create = new CreateChangeOperation(
						new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
						RefactoringStatus.FATAL);

				PerformChangeOperation perform = new PerformChangeOperation(create);

				ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());

				System.out.print("Recalculando metricas... ");

				MetricsUtils.waitForMetricsCalculate();

				System.out.println("Pronto!");

				ms = Dispatcher.getAbstractMetricSource(methodTargets.getMethod().getJavaProject().getPrimaryElement());
				metricsModified = MetricsUtils.getMetrics(ms);

				CSVUtils.writeCsvFile(
						methodTargets.getMethod().getCompilationUnit().getParent().getElementName() + "."
								+ methodTargets.getMethod().getDeclaringType().getElementName() + ":" + methodTargets.getMethod().getElementName() + " / "
								+ candidate.getType().getPackage().getName() + "." + candidate.getName(),
						metricsModified);

				Change undoChange = perform.getUndoChange();

				undoChange.perform(SingletonNullProgressMonitor.getNullProgressMonitor());

				System.out.print("Recalculando metricas... ");

				MetricsUtils.waitForMetricsCalculate();

				System.out.println("Pronto!");

				if (hadMetricsIncreased()) {
					System.out.println("Melhoram metricas");
					potentials.put(candidate, metricsModified);
					continue;
				} else {
					System.out.println("Pioram metricas");
				}

			}

		} catch (NullPointerException n) {
			System.out.println(n.getMessage());
		}

		if (foundBestTarget()) {
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

	public boolean foundBestTarget() {

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

	public MethodTargets getMethodTargets() {
		return methodTargets;
	}

	public void setMethodTargets(MethodTargets methodTargets) {
		this.methodTargets = methodTargets;
	}

}
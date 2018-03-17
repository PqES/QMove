package qmove.checker;

import java.util.ArrayList;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import qmove.metrics.qmood.calculator.QMOOD;
import qmove.persistence.MethodTargets;
import qmove.persistence.ValidMove;
import qmove.utils.MoveMethodUtils;
import qmove.utils.SingletonNullProgressMonitor;

public class MethodMetricsChecker {

	private double[] currentMetrics;
	private double[] newMetrics;
	private QMOOD qmood;

	public MethodMetricsChecker(QMOOD qmood) {
		this.qmood = qmood;
	}

	public ValidMove refactorAndCalculateMetrics(MethodTargets m) {

		try {

			ArrayList<ValidMove> validMoves = null;
			System.out.println("---------------------------------------------------");
			System.out.println("Analyzing method " + m.getMethod().getElementName() + " in "
					+ m.getMethod().getDeclaringType().getElementName());

			// Array for already found targets for method
			ArrayList<IVariableBinding> arrayTargets = MoveMethodUtils.getTargets(m.getMethod(), m.getTargets());

			// loop for each target that method go and come back
			for (IVariableBinding candidate : arrayTargets) {

				System.out.print("Going to " + candidate.getType().getName() + "... ");
				PerformChangeOperation perform = MoveMethodUtils.moveCandidate(m.getMethod(), candidate);
				System.out.println("OK");

				// get method and candidate classes and recalculate metrics
				ArrayList<String> types = new ArrayList<String>();
				types.add(m.getMethod().getDeclaringType().getFullyQualifiedName());
				types.add(candidate.getType().getQualifiedName());

				qmood.recalculateMetrics(types);
				newMetrics = qmood.getQMOODAttributes();
				printMetrics(qmood);

				// undo move method
				System.out.print("Returning method to original class... ");
				perform.getUndoChange().perform(SingletonNullProgressMonitor.getNullProgressMonitor());
				System.out.println("OK");

				// recalculate metrics after method come back
				qmood.recalculateMetrics(types);
				printMetrics(qmood);

				// verify if metrics increased
				if (hadMetricsIncreased()) {
					System.out.println("Improves Metrics");

					if (validMoves == null) {
						validMoves = new ArrayList<ValidMove>();
					}

					validMoves.add(new ValidMove(m.getMethod(), candidate.getType().getQualifiedName(), currentMetrics,
							newMetrics, types));

				} else {
					System.out.println("Worsens the metrics");
				}

			}

			return foundBestTarget(validMoves);

		} catch (Exception e) {
			return null;
		}
	}

	private boolean hadMetricsIncreased() {

		double sumOriginal = 0, sumModified = 0;

		for (int i = 0; i <= 5; i++) {
			sumOriginal += currentMetrics[i];
			sumModified += newMetrics[i];
		}

		if (((sumModified - sumOriginal) / Math.abs(sumOriginal)) * 100 > 0) {
			return true;
		}

		return false;
	}

	private ValidMove foundBestTarget(ArrayList<ValidMove> potentials) {

		if (potentials.size() == 0 || potentials == null) {
			return null;
		}

		if (potentials.size() == 1) {
			return potentials.get(0);
		}

		else {

			ValidMove bestCandidate = potentials.get(0);

			for (int i = 1; i < potentials.size(); i++) {

				if (bestCandidate.getIncrease() < potentials.get(i).getIncrease()) {
					bestCandidate = potentials.get(i);
				}
			}

			return bestCandidate;
		}

	}

	public void setMetrics(double[] currentMetrics) {
		this.currentMetrics = currentMetrics;
	}

	private void printMetrics(QMOOD qmood) {
		System.out.print("EFE = " + qmood.getEfe());
		System.out.print(" EXT = " + qmood.getExt());
		System.out.print(" FLE = " + qmood.getFle());
		System.out.print(" FUN = " + qmood.getFun());
		System.out.print(" REU = " + qmood.getReu());
		System.out.println(" UND = " + qmood.getUnd());
	}
}
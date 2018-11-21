package qmove.checker;

import java.util.ArrayList;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;

import qmove.handlers.QMoveHandler;
import qmove.metrics.qmood.calculator.QMOOD;
import qmove.persistence.MethodTargets;
import qmove.persistence.ValidMove;
import qmove.utils.Log;
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
			Log.writeLog("---------------------------------------------------");
			Log.writeLog("Analyzing method " + m.getMethod().getElementName() + " in "
					+ m.getMethod().getDeclaringType().getElementName());

			// Array for already found targets for method
			ArrayList<IVariableBinding> arrayTargets = MoveMethodUtils.getTargets(m.getMethod(), m.getTargets());

			// loop for each target that method go and come back
			for (IVariableBinding candidate : arrayTargets) {

				Log.writeLog("Going to " + candidate.getType().getName() + "... ");
				PerformChangeOperation perform = MoveMethodUtils.moveCandidate(m.getMethod(), candidate);
				Log.writeLog("OK");

				// get method and candidate classes and recalculate metrics
				ArrayList<String> types = new ArrayList<String>();
				types.add(m.getMethod().getDeclaringType().getFullyQualifiedName());
				types.add(candidate.getType().getQualifiedName());

				qmood.recalculateMetrics(types);
				newMetrics = qmood.getQMOODAttributes();
				printMetrics(qmood);

				// undo move method
				Log.writeLog("Returning method to original class... ");
				perform.getUndoChange().perform(SingletonNullProgressMonitor.getNullProgressMonitor());
				Log.writeLog("OK");

				// recalculate metrics after method come back
				qmood.recalculateMetrics(types);
				printMetrics(qmood);

				// verify if metrics increased
				if (hadMetricsIncreased()) {
					Log.writeLog("Improves Metrics");

					if (validMoves == null) {
						validMoves = new ArrayList<ValidMove>();
					}

					validMoves.add(new ValidMove(m.getMethod(), candidate.getType().getQualifiedName(), currentMetrics,
							newMetrics, types));

				} else {
					Log.writeLog("Worsens the metrics");
				}

			}

			return foundBestTarget(validMoves);

		} catch (Exception e) {
			return null;
		}
	}

	private boolean hadMetricsIncreased() {
		
		// Absolute Calibration #1
		if(QMoveHandler.calibrationType.equals("Abs#1")){
			
			for(int i=0; i <= 5; i++){
				if(newMetrics[i] - currentMetrics[i] < 0){
					return false;
				}
			}
			
			for(int i=0; i <= 5; i++){
				if(newMetrics[i] - currentMetrics[i] > 0){
					return true;
				}
			}
			
		}
		
		// Relative Calibration #1
		else if(QMoveHandler.calibrationType.equals("Rel#1")){
			
			for(int i=0; i <= 5; i++){
				if(((newMetrics[i] - currentMetrics[i])/ Math.abs(currentMetrics[i]))*100 < 0){
					return false;
				}
			}
			
			for(int i=0; i <= 5; i++){
				if(((newMetrics[i] - currentMetrics[i])/ Math.abs(currentMetrics[i]))*100 > 0){
					return true;
				}
			}
			
		}
		
		//Absolute Calibration #2
		else if(QMoveHandler.calibrationType.equals("Abs#2")){
			
			for(int i=1; i <= 5; i++){
				if(newMetrics[i] - currentMetrics[i] < 0){
					return false;
				}
			}
			
			for(int i=1; i <= 5; i++){
				if(newMetrics[i] - currentMetrics[i] > 0){
					return true;
				}
			}		
		}
			
		// Relative Calibration #2
		else if(QMoveHandler.calibrationType.equals("Rel#2")){
			
			for(int i=1; i <= 5; i++){
				if(((newMetrics[i] - currentMetrics[i])/ Math.abs(currentMetrics[i]))*100 < 0){
					return false;
				}
			}
			
			for(int i=1; i <= 5; i++){
				if(((newMetrics[i] - currentMetrics[i])/ Math.abs(currentMetrics[i]))*100 > 0){
					return true;
				}
			}
		}
		
		// Absolute Calibration #3		
		else if(QMoveHandler.calibrationType.equals("Abs#3")){
			
			double sumOriginal=0, sumModified=0;
			
			for(int i=0; i <= 5; i++){
				sumOriginal +=currentMetrics[i];
				sumModified +=newMetrics[i];
			}
				
			if(sumModified > sumOriginal){
				return true;
			}
					
			return false;
			
		}
				
		// Relative Calibration #3
		else if(QMoveHandler.calibrationType.equals("Rel#3")){
			
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
				
		// Absolute Calibration #4
		else if(QMoveHandler.calibrationType.equals("Abs#4")){
			
			if(newMetrics[1] < currentMetrics[1]
					|| newMetrics[2] < currentMetrics[2]
					|| newMetrics[5] < currentMetrics[5]){
					return false;
				} else if(newMetrics[1] == currentMetrics[1]
						&& newMetrics[2] == currentMetrics[2]
						&& newMetrics[5] == currentMetrics[5]){
					return false;
				} else {
					return true;
				}
		}
				
		// Relative Calibration #4
		else if(QMoveHandler.calibrationType.equals("Rel#4")){
			
			if(((newMetrics[1] - currentMetrics[1]) / Math.abs(currentMetrics[1]))*100 < 0 
					|| ((newMetrics[2] - currentMetrics[2]) / Math.abs(currentMetrics[2]))*100 < 0
					|| ((newMetrics[5] - currentMetrics[5]) / Math.abs(currentMetrics[5]))*100 < 0){
					return false;
				} else if(((newMetrics[1] - currentMetrics[1]) / Math.abs(currentMetrics[1]))*100 == 0
						&& ((newMetrics[2] - currentMetrics[2]) / Math.abs(currentMetrics[2]))*100 == 0
						&& ((newMetrics[5] - currentMetrics[5]) / Math.abs(currentMetrics[5]))*100 == 0){
					return false;
				} else {
					return true;
			}
			
		}
				
		// Absolute Calibration #5
		else if(QMoveHandler.calibrationType.equals("Abs#5")){
			
			if(newMetrics[6] < currentMetrics[6]
					|| newMetrics[7] < currentMetrics[7]
					|| newMetrics[8] > currentMetrics[8]){
					return false;
				} else if(newMetrics[6] == currentMetrics[6]
						&& newMetrics[7] == currentMetrics[7]
						&& newMetrics[8] == currentMetrics[8]){
					return false;
				} else {
					return true;
				}
		}
				
		// Relative Calibration #5
		else if(QMoveHandler.calibrationType.equals("Rel#5")){
			
			if(((newMetrics[6]-currentMetrics[6])/Math.abs(currentMetrics[6]))*100 < 0
					|| ((newMetrics[7]-currentMetrics[7])/Math.abs(currentMetrics[7]))*100 < 0
					|| ((currentMetrics[8]-newMetrics[8])/Math.abs(newMetrics[8]))*100 < 0){
					return false;
				} else if(((newMetrics[6]-currentMetrics[6])/Math.abs(currentMetrics[6]))*100 == 0
						&& ((newMetrics[7]-currentMetrics[7])/Math.abs(currentMetrics[7]))*100 == 0
						&& ((currentMetrics[8]-newMetrics[8])/Math.abs(newMetrics[8]))*100 < 0){
					return false;
				} else {
					return true;
			}
			
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
		Log.writeLog("EFE = " + qmood.getEfe() + " EXT = " + qmood.getExt() + " FLE = " + qmood.getFle() + " FUN = "
				+ qmood.getFun() + " REU = " + qmood.getReu() + " UND = " + qmood.getUnd());
	}
}
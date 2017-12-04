package qmove.movemethod;

import org.eclipse.jdt.core.dom.IVariableBinding;

import net.sourceforge.metrics.core.Metric;

public class MethodMetric {

	private IVariableBinding potential;
	private double[] metrics;
	
	public MethodMetric(IVariableBinding potential, double[] metrics){
		this.potential = potential;
		this.metrics = metrics;
	}
	
	
	public IVariableBinding getPotential() {
		return potential;
	}


	public double[] getMetrics() {
		return metrics;
	}
	
	public double getMetric(int i) {
		return metrics[i];
	}
	
	public double getIncreasedMetricsSum(double[] metricsOriginal){
		
		double sum=0;
		
		for(int i=0; i<metricsOriginal.length; i++){
			sum += (metrics[i] - metricsOriginal[i]);
		}
		
		return sum;
	}
	
	public boolean hasBetterMetricsThan(double[] metricsOriginal, double[] candidateMetrics){
		//Calibracao Relativa 5
		double increaseCISActual = ((metrics[11]-metricsOriginal[11])/Math.abs(metricsOriginal[11]))*100;
		double increaseCAMActual = ((metrics[14]-metricsOriginal[14])/Math.abs(metricsOriginal[14]))*100;
		double increaseDCCActual = ((metricsOriginal[15]-metrics[15])/Math.abs(metrics[15]))*100;
		double increaseActual = increaseCISActual+increaseCAMActual+increaseDCCActual;
		
		double increaseCISCandidate = ((candidateMetrics[11]-metricsOriginal[11])/Math.abs(metricsOriginal[11]))*100;
		double increaseCAMCandidate = ((candidateMetrics[14]-metricsOriginal[14])/Math.abs(metricsOriginal[14]))*100;
		double increaseDCCCandidate = ((metricsOriginal[15]-candidateMetrics[15])/Math.abs(candidateMetrics[15]))*100;
		double increaseCandidate = increaseCISCandidate+increaseCAMCandidate+increaseDCCCandidate;
			
		if(increaseActual > increaseCandidate){
			return true;
		}
		
		return false;
	}

	
}

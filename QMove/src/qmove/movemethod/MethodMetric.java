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
		//Calibracao Relativa 3
		double sumMetricsOriginal = metricsOriginal[0]+metricsOriginal[1]+metricsOriginal[2]+metricsOriginal[3]+metricsOriginal[4]+metricsOriginal[5];
		double sumMetrics = metrics[0]+metrics[1]+metrics[2]+metrics[3]+metrics[4]+metrics[5];
		double sumCandidateMetrics = candidateMetrics[0]+candidateMetrics[1]+candidateMetrics[2]+candidateMetrics[3]+candidateMetrics[4]+candidateMetrics[5];
		double percentageActual = ((sumMetrics-sumMetricsOriginal)/Math.abs(sumMetricsOriginal))*100;
		double percentageCandidate = ((sumCandidateMetrics-sumMetricsOriginal)/Math.abs(sumMetricsOriginal))*100;
		
		if(percentageActual > percentageCandidate){
			return true;
		}
		
		return false;
	}

	
}

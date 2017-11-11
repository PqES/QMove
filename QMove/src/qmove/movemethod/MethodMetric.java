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
	
	public boolean hasBetterMetricsThan(double[] candidateMetrics){
		//Calibracao 5
		if(metrics[11] >= candidateMetrics[11]
			&& metrics[14] > candidateMetrics[14]
			&& metrics[15] < candidateMetrics[15]){
				return true;
			} else {
				return false;
			}
	}

	
}

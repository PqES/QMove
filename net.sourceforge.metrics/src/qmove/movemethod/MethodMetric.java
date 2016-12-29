package qmove.movemethod;

import org.eclipse.jdt.core.dom.IVariableBinding;

import net.sourceforge.metrics.core.Metric;

public class MethodMetric {

	private IVariableBinding potential;
	private Metric[] metrics;
	
	public MethodMetric(IVariableBinding potential, Metric[] metrics){
		this.potential = potential;
		this.metrics = metrics;
	}
	
	
	public IVariableBinding getPotential() {
		return potential;
	}


	public Metric[] getMetrics() {
		return metrics;
	}
	
	public double getMetric(int i) {
		return metrics[i].getValue();
	}
	
	public double getIncreasedMetricsSum(Metric[] metricsOriginal){
		
		double sum=0;
		
		for(int i=0; i<metricsOriginal.length; i++){
			sum += (metrics[i].getValue() - metricsOriginal[i].getValue());
		}
		
		return sum;
	}

	
}

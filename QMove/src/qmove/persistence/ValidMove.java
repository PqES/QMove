package qmove.persistence;

import java.util.ArrayList;

import org.eclipse.jdt.core.IMethod;

public class ValidMove {

	private IMethod method;
	private String target;
	private double increase;
	private double[] oldMetrics;
	private double[] newMetrics;
	private ArrayList<String> types;

	public ValidMove(IMethod method, String target, double[] currentMetrics, double[] newMetrics,
			ArrayList<String> types) {
		this.method = method;
		this.target = target;
		this.types = types;
		this.oldMetrics = currentMetrics;
		this.newMetrics = newMetrics;
		this.increase = calculeIncrease(currentMetrics, newMetrics);
	}

	public ArrayList<String> getTypes() {
		return types;
	}

	public IMethod getMethod() {
		return method;
	}

	public String getTarget() {
		return target;
	}

	public double getIncrease() {
		return increase;
	}

	public double[] getOldMetrics() {
		return oldMetrics;
	}

	public double[] getNewMetrics() {
		return newMetrics;
	}

	public double calculeIncrease(double[] metricsOriginal, double[] metrics) {
		double sumMetricsOriginal = (metricsOriginal[0] + metricsOriginal[1] + metricsOriginal[2] + metricsOriginal[3]
				+ metricsOriginal[4] + metricsOriginal[5]);

		double sumNewMetrics = (metrics[0] + metrics[1] + metrics[2] + metrics[3] + metrics[4] + metrics[5]);

		double percentageIncrease = ((sumNewMetrics - sumMetricsOriginal) / Math.abs(sumMetricsOriginal)) * 100;

		return percentageIncrease;

	}
}
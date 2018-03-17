package qmove.persistence;

public class BetterMethods {

	private String method;
	private String target;
	private double increase;
	private double[] newMetrics;

	public BetterMethods(String method, String target, double increase, double[] newMetrics) {
		this.method = method;
		this.target = target;
		this.increase = increase;
		this.newMetrics = newMetrics;
	}

	public String getMethod() {
		return method;
	}

	public String getTarget() {
		return target;
	}

	public double getIncrease() {
		return increase;
	}

	public double[] getNewMetrics() {
		return newMetrics;
	}
}

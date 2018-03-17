package qmove.metrics.qmood.attributes;

public class Reusability {

	public static double calcule(double[] metrics) {
		double reu = -0.25 * metrics[0] // DCC
				+ 0.25 * metrics[1] // CAM
				+ 0.50 * metrics[2] // CIS
				+ 0.50 * metrics[3]; // DSC

		return reu;
	}
}

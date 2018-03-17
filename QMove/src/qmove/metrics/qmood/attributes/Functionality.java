package qmove.metrics.qmood.attributes;

public class Functionality {

	public static double calcule(double[] metrics) {
		double fun = 0.12 * metrics[0] // CAM
				+ 0.22 * metrics[1] // NOPM
				+ 0.22 * metrics[2] // CIS
				+ 0.22 * metrics[3] // DSC
				+ 0.22 * metrics[4]; // NOH
		return fun;
	}
}

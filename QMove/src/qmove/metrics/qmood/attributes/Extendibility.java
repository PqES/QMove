package qmove.metrics.qmood.attributes;

public class Extendibility {

	public static double calcule(double[] metrics) {
		double ext = 0.5 * metrics[0] // ANA
				- 0.5 * metrics[1] // DCC
				+ 0.5 * metrics[2] // MFA
				+ 0.5 * metrics[3]; // NOPM
		return ext;
	}
}

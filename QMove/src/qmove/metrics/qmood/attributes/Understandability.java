package qmove.metrics.qmood.attributes;

public class Understandability {

	public static double calcule(double[] metrics) {
		double und = -0.33 * metrics[0] // ANA
				+ 0.33 * metrics[1] // DAM
				- 0.33 * metrics[2] // DCC
				+ 0.33 * metrics[3] // CAM
				- 0.33 * metrics[4] // NOPM
				- 0.33 * metrics[5] // NONM
				- 0.33 * metrics[6]; // DSC
		return und;
	}
}

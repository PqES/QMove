package qmove.metrics.qmood.attributes;

public class Flexibility {

	public static double calcule(double metrics[]) {
		double fle = 0.25 * metrics[0] // DAM
				- 0.25 * metrics[1] // DCC
				+ 0.50 * metrics[2] // MOA
				+ 0.50 * metrics[3]; // NOPM
		return fle;
	}
}

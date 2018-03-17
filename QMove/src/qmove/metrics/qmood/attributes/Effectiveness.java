package qmove.metrics.qmood.attributes;

public class Effectiveness {

	public static double calcule(double metrics[]) {

		double efe = 0.2 * metrics[0] // ANA
				+ 0.2 * metrics[1] // DAM
				+ 0.2 * metrics[2] // MOA
				+ 0.2 * metrics[3] // MFA
				+ 0.2 * metrics[4]; // NOPM
		return efe;
	}

}

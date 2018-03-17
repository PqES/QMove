package qmove.persistence;

import java.util.ArrayList;

public class Recommendation {

	private int id;
	private String method;
	private String[] parameters;
	private String target;
	private double[] oldMetrics;
	private double[] newMetrics;
	private double increase;
	private ArrayList<BetterMethods> listBetterMethods;

	public Recommendation(int id, String method, String target, String[] parameters, double[] oldMetrics,
			double[] newMetrics, double increase, ArrayList<BetterMethods> listBetterMethods) {
		this.id = id;
		this.method = method;
		this.parameters = parameters;
		this.target = target;
		this.oldMetrics = oldMetrics;
		this.newMetrics = newMetrics;
		this.increase = increase;
		this.listBetterMethods = listBetterMethods;
	}

	public int getId() {
		return id;
	}

	public String getMethod() {
		return method;
	}

	public String[] getParameters() {
		return parameters;
	}

	public String getTarget() {
		return target;
	}

	public ArrayList<BetterMethods> getListBetterMethods() {
		return listBetterMethods;
	}

	public double[] getOldMetrics() {
		return oldMetrics;
	}

	public double[] getNewMetrics() {
		return newMetrics;
	}

	public double getIncrease() {
		return increase;
	}

	public String[][] getMethodsMetrics() {

		int rows = listBetterMethods.size() + 1;
		double media = 0, aux = 0;
		String s[][] = new String[rows][8];
		String sAux[] = getCurrentMetrics();
		s[0][0] = "Current";
		for (int i = 1; i < getCurrentMetrics().length; i++) {
			s[0][i] = sAux[i];
		}

		s[0][7] = String.format("%.5f", calculeMediaCurrent());

		int i = 1;
		for (BetterMethods bm : listBetterMethods) {

			s[i][0] = "[" + bm.getMethod() + ", " + bm.getTarget() + "]";

			for (int j = 1; j <= 6; j++) {

				aux = bm.getNewMetrics()[j - 1] - oldMetrics[j - 1];

				if (aux < 0)
					s[i][j] = String.format("%.5f", aux);
				else if (aux > 0)
					s[i][j] = String.format("+%.5f", aux);
				else if (aux == 0)
					s[i][j] = "";

				media += bm.getNewMetrics()[j - 1];
			}

			media = (media / 6) - calculeMediaCurrent();
			if (media < 0)
				s[i][7] = String.format("%.5f", aux);
			else if (media > 0)
				s[i][7] = String.format("+%.5f", aux);
			else if (media == 0)
				s[i][7] = "0";
			i++;
		}

		return s;
	}

	private String[] getCurrentMetrics() {
		String s[] = new String[8];
		s[0] = "Current";
		s[1] = String.format("%.5f", oldMetrics[0]);
		s[2] = String.format("%.5f", oldMetrics[1]);
		s[3] = String.format("%.5f", oldMetrics[2]);
		s[4] = String.format("%.5f", oldMetrics[3]);
		s[5] = String.format("%.5f", oldMetrics[4]);
		s[6] = String.format("%.5f", oldMetrics[5]);
		double media = calculeMediaCurrent();
		s[7] = String.format("%.5f", media);

		return s;
	}

	private double calculeMediaCurrent() {
		double media = (oldMetrics[0] + oldMetrics[1] + oldMetrics[2] + oldMetrics[3] + oldMetrics[4] + oldMetrics[5])
				/ 6;
		return media;

	}

}
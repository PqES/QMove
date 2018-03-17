package qmove.persistence;

public class Metrics {
	private double dsc, noh, ana, dam, dcc, cam, moa, mfa, nop, cis, nom;

	public Metrics(double dsc, double noh, double ana, double dam, double dcc, double cam, double moa, double mfa,
			double nop, double cis, double nom) {
		this.dsc = dsc;
		this.noh = noh;
		this.ana = ana;
		this.dam = dam;
		this.dcc = dcc;
		this.cam = cam;
		this.moa = moa;
		this.mfa = mfa;
		this.nop = nop;
		this.cis = cis;
		this.nom = nom;
	}

	public double getDsc() {
		return dsc;
	}

	public void setDsc(double dsc) {
		this.dsc = dsc;
	}

	public double getNoh() {
		return noh;
	}

	public void setNoh(double noh) {
		this.noh = noh;
	}

	public double getAna() {
		return ana;
	}

	public void setAna(double ana) {
		this.ana = ana;
	}

	public double getDam() {
		return dam;
	}

	public void setDam(double dam) {
		this.dam = dam;
	}

	public double getDcc() {
		return dcc;
	}

	public void setDcc(double dcc) {
		this.dcc = dcc;
	}

	public double getCam() {
		return cam;
	}

	public void setCam(double cam) {
		this.cam = cam;
	}

	public double getMoa() {
		return moa;
	}

	public void setMoa(double moa) {
		this.moa = moa;
	}

	public double getMfa() {
		return mfa;
	}

	public void setMfa(double mfa) {
		this.mfa = mfa;
	}

	public double getNop() {
		return nop;
	}

	public void setNop(double nop) {
		this.nop = nop;
	}

	public double getCis() {
		return cis;
	}

	public void setCis(double cis) {
		this.cis = cis;
	}

	public double getNom() {
		return nom;
	}

	public void setNom(double nom) {
		this.nom = nom;
	}
}

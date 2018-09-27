package qmove.metrics.qmood.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import qmove.handlers.QMoveHandler;
import qmove.metrics.qmood.attributes.Effectiveness;
import qmove.metrics.qmood.attributes.Extendibility;
import qmove.metrics.qmood.attributes.Flexibility;
import qmove.metrics.qmood.attributes.Functionality;
import qmove.metrics.qmood.attributes.Reusability;
import qmove.metrics.qmood.attributes.Understandability;
import qmove.metrics.qmood.properties.AverageNumberOfAncestors;
import qmove.metrics.qmood.properties.ClassInterfaceSize;
import qmove.metrics.qmood.properties.CohesionAmongMethodsOfClass;
import qmove.metrics.qmood.properties.DataAccessMetrics;
import qmove.metrics.qmood.properties.DesignSizeInClasses;
import qmove.metrics.qmood.properties.DirectClassCoupling;
import qmove.metrics.qmood.properties.MeasureOfAggregation;
import qmove.metrics.qmood.properties.MeasureOfFunctionalAbstraction;
import qmove.metrics.qmood.properties.NumberOfHierarchies;
import qmove.metrics.qmood.properties.NumberOfMethods;
import qmove.metrics.qmood.properties.NumberOfPolymorphicMethods;
import qmove.persistence.Metrics;
import qmove.utils.SingletonNullProgressMonitor;

public class QMOOD {

	private double dam = 0, noh = 0, ana = 0, dcc = 0, cam = 0, moa = 0, mfa = 0, nop = 0, cis = 0, nom = 0, dsc = 0,
			efe = 0, ext = 0, fle = 0, fun = 0, reu = 0, und = 0;

	private Map<String, Metrics> allClassesMetrics = new HashMap<String, Metrics>();

	public QMOOD(ArrayList<IType> types) {

		calculeQMOODProperties(types);
		calculeQMOODAttributes();
	}

	public void calculeQMOODProperties(ArrayList<IType> types) {
		for (int i = 0; i < types.size(); i++) {

			allClassesMetrics.put(types.get(i).getFullyQualifiedName(), new Metrics(DesignSizeInClasses.calcule(types),
					NumberOfHierarchies.calcule(types.get(i)), AverageNumberOfAncestors.calcule(types.get(i)),
					DataAccessMetrics.calcule(types.get(i)), DirectClassCoupling.calcule(types.get(i)),
					CohesionAmongMethodsOfClass.calcule(types.get(i)), MeasureOfAggregation.calcule(types.get(i)),
					MeasureOfFunctionalAbstraction.calcule(types.get(i)),
					NumberOfPolymorphicMethods.calcule(types.get(i)), ClassInterfaceSize.calcule(types.get(i)),
					NumberOfMethods.calcule(types.get(i))));
		}
	}

	public void calculeQMOODAttributes() {

		dsc = allClassesMetrics.size();

		Metrics m;
		for (String clazz : allClassesMetrics.keySet()) {
			m = allClassesMetrics.get(clazz);
			dam += m.getDam();
			noh += m.getNoh();
			ana += m.getAna();
			dcc += m.getDcc();
			cam += m.getCam();
			moa += m.getMoa();
			mfa += m.getMfa();
			nop += m.getNop();
			cis += m.getCis();
			nom += m.getNom();
		}

		calculateAttributes();

	}

	public void recalculateMetrics(ArrayList<String> types) {

		// old qmood properties values
		double oldDam = 0, oldNoh = 0, oldAna = 0, oldDcc = 0, oldCam = 0, oldMoa = 0, oldMfa = 0, oldNop = 0,
				oldCis = 0, oldNom = 0, newDam = 0, newNoh = 0, newAna = 0, newDcc = 0, newCam = 0, newMoa = 0,
				newMfa = 0, newNop = 0, newCis = 0, newNom = 0;

		for (String typeName : types) {

			// get class modified from project
			IType type = getIType(typeName);

			// get old metrics
			Metrics m = allClassesMetrics.get(type.getFullyQualifiedName());

			// store old metrics
			oldDam += m.getDam();
			oldNoh += m.getNoh();
			oldAna += m.getAna();
			oldDcc += m.getDcc();
			oldCam += m.getCam();
			oldMoa += m.getMoa();
			oldMfa += m.getMfa();
			oldNop += m.getNop();
			oldCis += m.getCis();
			oldNom += m.getNom();

			m.setDam(DataAccessMetrics.calcule(type));
			m.setNoh(NumberOfHierarchies.calcule(type));
			m.setAna(AverageNumberOfAncestors.calcule(type));
			m.setDcc(DirectClassCoupling.calcule(type));
			m.setCam(CohesionAmongMethodsOfClass.calcule(type));
			m.setMoa(MeasureOfAggregation.calcule(type));
			m.setMfa(MeasureOfFunctionalAbstraction.calcule(type));
			m.setNop(NumberOfPolymorphicMethods.calcule(type));
			m.setCis(ClassInterfaceSize.calcule(type));
			m.setNom(NumberOfMethods.calcule(type));

			// store new metrics
			newDam += m.getDam();
			newNoh += m.getNoh();
			newAna += m.getAna();
			newDcc += m.getDcc();
			newCam += m.getCam();
			newMoa += m.getMoa();
			newMfa += m.getMfa();
			newNop += m.getNop();
			newCis += m.getCis();
			newNom += m.getNom();
		}

		// subtract old values
		dam -= oldDam;
		noh -= oldNoh;
		ana -= oldAna;
		dcc -= oldDcc;
		cam -= oldCam;
		moa -= oldMoa;
		mfa -= oldMfa;
		nop -= oldNop;
		cis -= oldCis;
		nom -= oldNom;

		// add new values
		dam += newDam;
		noh += newNoh;
		ana += newAna;
		dcc += newDcc;
		cam += newCam;
		moa += newMoa;
		mfa += newMfa;
		nop += newNop;
		cis += newCis;
		nom += newNom;

		calculateAttributes();

	}

	private void calculateAttributes() {
		efe = Effectiveness.calcule(new double[] { ana / dsc, dam, moa, mfa, nop });
		ext = Extendibility.calcule(new double[] { ana / dsc, dcc, mfa, nop });
		fle = Flexibility.calcule(new double[] { dam, dcc, moa, nop });
		fun = Functionality.calcule(new double[] { cam, nop, cis, dsc, noh });
		reu = Reusability.calcule(new double[] { dcc, cam, cis, dsc });
		und = Understandability.calcule(new double[] { ana / dsc, dam, dcc, cam, nop, nom, dsc });
	}

	private IType getIType(String typeName) {
		try {
			return QMoveHandler.projectCopy.findType(typeName, SingletonNullProgressMonitor.getNullProgressMonitor());
		} catch (JavaModelException e) {
			return null;
		}
	}

	public double[] getQMOODAttributes() {
		return new double[] { efe, ext, fle, fun, reu, und, cis, cam, dcc };
	}

	public Map<String, Metrics> getAllMetrics() {
		return allClassesMetrics;
	}

	public double getDam() {
		return dam;
	}

	public void setDam(double dam) {
		this.dam = dam;
	}

	public double getNoh() {
		return noh;
	}

	public void setNoh(double noh) {
		this.noh = noh;
	}

	public double getAna() {
		return ana / dsc;
	}

	public void setAna(double ana) {
		this.ana = ana;
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

	public double getDsc() {
		return dsc;
	}

	public void setDsc(double dsc) {
		this.dsc = dsc;
	}

	public double getEfe() {
		return efe;
	}

	public void setEfe(double efe) {
		this.efe = efe;
	}

	public double getExt() {
		return ext;
	}

	public void setExt(double ext) {
		this.ext = ext;
	}

	public double getFle() {
		return fle;
	}

	public void setFle(double fle) {
		this.fle = fle;
	}

	public double getFun() {
		return fun;
	}

	public void setFun(double fun) {
		this.fun = fun;
	}

	public double getReu() {
		return reu;
	}

	public void setReu(double reu) {
		this.reu = reu;
	}

	public double getUnd() {
		return und;
	}

	public void setUnd(double und) {
		this.und = und;
	}

}

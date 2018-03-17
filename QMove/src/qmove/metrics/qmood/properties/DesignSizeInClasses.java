package qmove.metrics.qmood.properties;

import java.util.ArrayList;

import org.eclipse.jdt.core.IType;

public class DesignSizeInClasses {

	public static double calcule(ArrayList<IType> types) {
		double dsc;
		dsc = types.size();
		return dsc;
	}
}
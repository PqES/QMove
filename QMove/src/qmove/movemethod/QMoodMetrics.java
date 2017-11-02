package qmove.movemethod;

import net.sourceforge.metrics.calculators.qmood.Effectiveness;
import net.sourceforge.metrics.calculators.qmood.Extendibility;
import net.sourceforge.metrics.calculators.qmood.Flexibility;
import net.sourceforge.metrics.calculators.qmood.Functionality;
import net.sourceforge.metrics.calculators.qmood.Reusability;
import net.sourceforge.metrics.calculators.qmood.Understandability;
import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

public class QMoodMetrics {
	

	public QMoodMetrics(){

	}

	public static double[] getQMoodMetrics(final AbstractMetricSource ms){

		double[] metrics = new double[6];

		try {

			if (ms == null) {
				return metrics = null;
			}			

			MetricsPlugin plugin = MetricsPlugin.getDefault();
			String[] names = plugin.getMetricIds();
			int j=0;
			for (int i = 0; i < names.length; i++) {

				if (names[i].matches("FLE")
						|| names[i].matches("EFE")
						|| names[i].matches("EXT")
						|| names[i].matches("FUN")
						|| names[i].matches("REU")
						|| names[i].matches("ENT")){

					Metric m = ms.getValue(names[i]);
					metrics[j] = m.getValue();
					System.out.print(m.getName() + ": ");
					System.out.print(m.getValue() + " ");
					j++;
				}			 
			} 

			System.out.println();

		} catch (Throwable e) {
			Log.logError("MetricsTable::setMetrics", e);
		}
		return metrics;
	}
	
public static double[] getMetrics(final AbstractMetricSource ms){
		
		double[] metrics = new double[17];
		
		Effectiveness efe = new Effectiveness();
		Extendibility ext = new Extendibility();
		Flexibility fle = new Flexibility();
		Functionality fun = new Functionality();
		Reusability reu = new Reusability();
		Understandability und = new Understandability();
			
		metrics[2] = efe.calculateEfe(ms);
		metrics[3] =  ext.calculateExt(ms);
		metrics[1] = fle.calculateFle(ms);
		metrics[4] = fun.calculateFun(ms);
		metrics[0] = reu.calculateReu(ms);
		metrics[5] = und.calculateUnd(ms);
		

		System.out.print("REU: "+metrics[0]);
		System.out.print(" FLE: "+metrics[1]);
		System.out.print(" EFE: "+metrics[2]);
		System.out.print(" EXT: "+metrics[3]);
		System.out.print(" FUN: "+metrics[4]);
		System.out.println(" UND: "+metrics[5]);
		
		MetricsPlugin plugin = MetricsPlugin.getDefault();
		String[] names = plugin.getMetricIds();
		int j=6;
		for (int i = 0; i < names.length; i++) {

			if (names[i].matches("NOM")
					|| names[i].matches("NOP")
					|| names[i].matches("DSC")
					|| names[i].matches("ANA")
					|| names[i].matches("MFA")
					|| names[i].matches("CIS")
					|| names[i].matches("MOA")
					|| names[i].matches("DAM")
					|| names[i].matches("CAM")
					|| names[i].matches("DCC")
					|| names[i].matches("NOH")){

				Metric m = ms.getValue(names[i]);
				metrics[j] = m.getValue();
				System.out.print(m.getName() + ": ");
				System.out.print(m.getValue() + " ");
				j++;
			}			 
		}
		
		System.out.println();
		
		return metrics;
		
	}
}

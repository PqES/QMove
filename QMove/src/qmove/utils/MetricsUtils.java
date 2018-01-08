package qmove.utils;

import net.sourceforge.metrics.calculators.qmood.Effectiveness;
import net.sourceforge.metrics.calculators.qmood.Extendibility;
import net.sourceforge.metrics.calculators.qmood.Flexibility;
import net.sourceforge.metrics.calculators.qmood.Functionality;
import net.sourceforge.metrics.calculators.qmood.Reusability;
import net.sourceforge.metrics.calculators.qmood.Understandability;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

public class MetricsUtils {
	
	public static boolean queueIsZero;
	private static Effectiveness efe;
	private static Extendibility ext;
	private static Flexibility fle;
	private static Functionality fun;
	private static Reusability reu;
	private static Understandability und;
	private static boolean instancied = false;
	
	
	public static void waitForMetricsCalculate(){
		queueIsZero = false;
		while (queueIsZero == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static double[] getMetrics(final AbstractMetricSource ms){
		
		if(!instancied){
			efe = new Effectiveness();
			ext = new Extendibility();
			fle = new Flexibility();
			fun = new Functionality();
			reu = new Reusability();
			und = new Understandability();
			instancied = true;
		}
		
		double[] metrics = new double[6];
			
		metrics[2] = efe.calculateEfe(ms);
		metrics[3] = ext.calculateExt(ms);
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
		
		return metrics;
		
	}

}

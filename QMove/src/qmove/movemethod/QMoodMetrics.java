package qmove.movemethod;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

public class QMoodMetrics {

	public QMoodMetrics(){

	}

	public static Metric[] getQMoodMetrics(final AbstractMetricSource ms){

		Metric[] metrics = new Metric[6];

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
					metrics[j] = m;
					System.out.print(metrics[j].getName() + ": ");
					System.out.print(metrics[j].getValue() + " ");
					j++;
				}			 
			} 

			System.out.println();

		} catch (Throwable e) {
			Log.logError("MetricsTable::setMetrics", e);
		}
		return metrics;
	}
}

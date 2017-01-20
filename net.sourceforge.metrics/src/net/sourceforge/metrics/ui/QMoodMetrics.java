package net.sourceforge.metrics.ui;



import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;

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
	
	public  Metric[] getQMoodMetrics(ExecutionEvent event){
			
			TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
			IJavaElement je = (IJavaElement) selection.getFirstElement();
	  		AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
		
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

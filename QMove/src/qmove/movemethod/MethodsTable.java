package qmove.movemethod;

import java.io.Serializable;
import java.util.ArrayList;
import net.sourceforge.metrics.core.Metric;

public class MethodsTable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Metric[] currentMetrics;
	private ArrayList<MethodsChosen> methods;
	
	public MethodsTable(Metric[] metricsCurrent, ArrayList<MethodsChosen> methods){	
		
		this.currentMetrics = metricsCurrent;
		this.methods = methods;
	}
	
	public String[][] getMethodsMetrics(){
		
		int rows = methods.size();
		double media=0, aux=0;
		String s[][] = new String[rows+1][8];
		String sAux[] = getCurrentMetrics();
		for(int i =0; i < getCurrentMetrics().length; i++){
			s[0][i] = sAux[i];
		}
		
		for(int i=1; i <= rows; i++){
			
			s[i][0]= "["+ methods.get(i-1).getMethod().getElementName()
					+ ", "
					+ methods.get(i-1).getTargetChosen().getName()
					+ "]";
		
			for(int j=1;j<=6;j++){
				
				aux = methods.get(i-1).getMetrics()[j-1].getValue()
					  - currentMetrics[j-1].getValue();
				
				if(aux < 0) s[i][j] = String.format("%.2f",aux);
				else if(aux > 0) s[i][j] = String.format("+%.2f",aux);
				else if (aux == 0) s[i][j] = "";
				
				media +=  methods.get(i-1).getMetrics()[j-1].getValue();
			}
			
			media = (media/6) - calculeMediaCurrent();
			if(media < 0) s[i][7] = String.format("%.2f",aux);
			else if(media > 0) s[i][7] = String.format("+%.2f",aux);
			else if (media == 0) s[i][7] = "0";
			 
		}
		
		return s;
	}
	
	private String[] getCurrentMetrics(){
		String s[] = new String[8];
		s[0] = "Current";
		s[1] = String.format("%.2f", currentMetrics[0].getValue());
		s[2] = String.format("%.2f", currentMetrics[1].getValue());
		s[3] = String.format("%.2f", currentMetrics[2].getValue());
		s[4] = String.format("%.2f", currentMetrics[3].getValue());
		s[5] = String.format("%.2f", currentMetrics[4].getValue());
		s[6] = String.format("%.2f", currentMetrics[5].getValue());
		double media =  calculeMediaCurrent();
		s[7] = String.format("%.2f", media);
		
		return s;
	}
	
	private double calculeMediaCurrent(){
		double media = (currentMetrics[0].getValue() +
				currentMetrics[1].getValue() +
				currentMetrics[2].getValue() +
				currentMetrics[3].getValue() +
				currentMetrics[4].getValue() +
				currentMetrics[5].getValue())/6;
		return media;
		
	}	
	
}

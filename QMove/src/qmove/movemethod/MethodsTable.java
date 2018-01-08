package qmove.movemethod;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MethodsTable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] currentMetrics;
	private Map<String, double[]> methods = new HashMap<String, double[]>();
	//private ArrayList<String> methods;
	
	public MethodsTable(double[] currentMetrics, Map<String, double[]> methods){	
		
		this.currentMetrics = currentMetrics;
		this.methods = methods;
	}
	
	public String[][] getMethodsMetrics(){
		
		int rows = methods.size()+1;
		double media=0, aux=0;
		String s[][] = new String[rows][8];
		String sAux[] = getCurrentMetrics();
		for(int i =0; i < getCurrentMetrics().length; i++){
			s[0][i] = sAux[i];
		}
		
		for(int i=1; i <= rows; i++){
			
			s[i][0]= "["+methods.get(i-1)+"]";
		
			for(int j=1;j<=6;j++){
				
				aux = methods.get((String) methods.keySet().toArray()[i-1])[j-1]-currentMetrics[j-1];
				//aux = methods.get(i-1).getMetrics()[j-1]-currentMetrics[j-1];
				
				if(aux < 0) s[i][j] = String.format("%.5f",aux);
				else if(aux > 0) s[i][j] = String.format("+%.5f",aux);
				else if (aux == 0) s[i][j] = "";
				
				media +=  methods.get((String) methods.keySet().toArray()[i-1])[j-1];
			}
			
			media = (media/6) - calculeMediaCurrent();
			if(media < 0) s[i][7] = String.format("%.5f",aux);
			else if(media > 0) s[i][7] = String.format("+%.5f",aux);
			else if (media == 0) s[i][7] = "0";
			 
		}
		
		return s;
	}
	
	private String[] getCurrentMetrics(){
		String s[] = new String[8];
		s[0] = "Current";
		s[1] = String.format("%.5f", currentMetrics[0]);
		s[2] = String.format("%.5f", currentMetrics[1]);
		s[3] = String.format("%.5f", currentMetrics[2]);
		s[4] = String.format("%.5f", currentMetrics[3]);
		s[5] = String.format("%.5f", currentMetrics[4]);
		s[6] = String.format("%.5f", currentMetrics[5]);
		double media =  calculeMediaCurrent();
		s[7] = String.format("%.5f", media);
		
		return s;
	}
	
	private double calculeMediaCurrent(){
		double media = (currentMetrics[0] +
				currentMetrics[1] +
				currentMetrics[2] +
				currentMetrics[3] +
				currentMetrics[4] +
				currentMetrics[5])/6;
		return media;
		
	}	
	
}

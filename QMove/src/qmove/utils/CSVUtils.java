package qmove.utils;

import java.io.FileWriter;
import java.io.IOException;

public class CSVUtils {
	
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
		
	public static void initializeCsvFile() {
		
		FileWriter fileWriter = null;
		
		try {
			
			fileWriter = new FileWriter(System.getProperty("user.home") + "/dataMetrics.csv", true);

			
			fileWriter.append("METHOD / TARGET");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("REU");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("FLE");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("EFE");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("EXT");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("FUN");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("UND");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("NOM");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("NOP");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("DSC");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("ANA");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("MFA");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("CIS");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("MOA");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("DAM");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("CAM");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("DCC");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("NOH");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append("SUM");
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append(NEW_LINE_SEPARATOR);
					
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	public static void writeCsvFile(String method, double[] metrics) {
		
		FileWriter fileWriter = null;
		
		try {
			
			fileWriter = new FileWriter(System.getProperty("user.home") + "/dataMetrics.csv", true);

			fileWriter.append(method);
			fileWriter.append(COMMA_DELIMITER);
			
			double sum = metrics[0] + metrics[1] + metrics[2] + metrics[3] + metrics[4] + metrics[5];
			
			for (int i=0; i<metrics.length; i++) {
				fileWriter.append(String.valueOf(metrics[i]));
				fileWriter.append(COMMA_DELIMITER);				
			}
			
			fileWriter.append(String.valueOf(sum));
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append(NEW_LINE_SEPARATOR);
					
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	
	
	public static void writeRecInFile(int id, String method, String target, double increase) {
		
		FileWriter fileWriter = null;
		
		try {
			
			fileWriter = new FileWriter(System.getProperty("user.home") + "/recommendations.csv", true);

			fileWriter.append(String.valueOf(id));
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append(method);
			fileWriter.append(COMMA_DELIMITER);				
			
			
			fileWriter.append(target);
			fileWriter.append(COMMA_DELIMITER);				
			
			
			fileWriter.append(String.valueOf(increase));
			fileWriter.append(COMMA_DELIMITER);
			
			fileWriter.append(NEW_LINE_SEPARATOR);
					
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
		}
			
	}	
}

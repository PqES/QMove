package qmove.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import qmove.persistence.BetterMethods;
import qmove.persistence.Recommendation;

public class FileUtils {

	private static final String COMMA_DELIMITER = ";";
	private static final String SPECIAL_DELIMITER = "#";
	private static final String SPECIAL_SEPARATOR = "@";
	private static final String NEW_LINE_SEPARATOR = "\n";

	public static void writeValidMoveMethod(String method, String target, double increase) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(System.getProperty("user.dir") + "/refactorings.csv", true);

			fileWriter.append(method);
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(target);
			fileWriter.append(COMMA_DELIMITER);

			fileWriter.append(Double.toString(increase));

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

	public static void writeBetterMethod(String method, String target, double increase, double[] newMetrics) {

		FileWriter fileWriter = null;

		try {

			fileWriter = new FileWriter(System.getProperty("user.dir") + "/betterMethods.csv", true);

			// write method name on file
			fileWriter.append(method);
			fileWriter.append(COMMA_DELIMITER);

			// wrtire target name on file
			fileWriter.append(target);
			fileWriter.append(COMMA_DELIMITER);

			// write increase on file
			fileWriter.append(Double.toString(increase));
			fileWriter.append(COMMA_DELIMITER);

			// write new metrics on file
			for (int i = 0; i < newMetrics.length; i++) {
				fileWriter.append(Double.toString(newMetrics[i]));
				fileWriter.append(COMMA_DELIMITER);
			}

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

	public static void writeRecommendation(int id, String method, String target, double[] oldMetrics,
			double[] newMetrics, double increase, String[] parameters) {

		FileWriter fileWriter = null;

		try {

			// create or open recommendations file
			fileWriter = new FileWriter(System.getProperty("user.dir") + "/recommendations.csv", true);

			// write id on file
			fileWriter.append(Integer.toString(id));
			fileWriter.append(COMMA_DELIMITER);

			// write method on file
			fileWriter.append(method);
			fileWriter.append(COMMA_DELIMITER);

			// write target on file
			fileWriter.append(target);
			fileWriter.append(COMMA_DELIMITER);

			// write old metrics on file
			for (int i = 0; i < oldMetrics.length; i++) {
				fileWriter.append(Double.toString(oldMetrics[i]));
				fileWriter.append(COMMA_DELIMITER);
			}

			// write new metrics on file
			for (int i = 0; i < newMetrics.length; i++) {
				fileWriter.append(Double.toString(newMetrics[i]));
				fileWriter.append(COMMA_DELIMITER);
			}

			// write increase on file
			fileWriter.append(Double.toString(increase));
			fileWriter.append(COMMA_DELIMITER);
			
			// write method parameters
			if(parameters.length > 0){
				for(String parameter : parameters){
					fileWriter.append(parameter);
				}
			}

			// write better methods list on file
			ArrayList<BetterMethods> list = readBetterMethodsFile();
			for (BetterMethods bm : list) {

				fileWriter.append(bm.getMethod());
				fileWriter.append(SPECIAL_DELIMITER);

				fileWriter.append(bm.getTarget());
				fileWriter.append(SPECIAL_DELIMITER);

				fileWriter.append(Double.toString(bm.getIncrease()));
				fileWriter.append(SPECIAL_DELIMITER);

				for (int i = 0; i < bm.getNewMetrics().length; i++) {
					fileWriter.append(Double.toString(bm.getNewMetrics()[i]));
					fileWriter.append(SPECIAL_DELIMITER);
				}

				fileWriter.append(SPECIAL_SEPARATOR);
			}

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

	@SuppressWarnings("resource")
	private static ArrayList<BetterMethods> readBetterMethodsFile() {
		ArrayList<BetterMethods> listBetterMethods = null;
		try {

			String[] array;
			File f = new File(System.getProperty("user.dir") + "/betterMethods.csv");
			BufferedReader b = new BufferedReader(new FileReader(f));
			String readLine = "";
			while ((readLine = b.readLine()) != null) {

				array = readLine.split(";");

				if (listBetterMethods == null) {
					listBetterMethods = new ArrayList<BetterMethods>();
				}

				listBetterMethods.add(new BetterMethods(array[0], array[1], Double.parseDouble(array[2]),
						new double[] { Double.parseDouble(array[3]), Double.parseDouble(array[4]),
								Double.parseDouble(array[5]), Double.parseDouble(array[6]),
								Double.parseDouble(array[7]), Double.parseDouble(array[8]) }));

			}

			f.delete();

			return listBetterMethods;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return listBetterMethods;
		} catch (IOException e) {
			e.printStackTrace();
			return listBetterMethods;
		}
	}

	@SuppressWarnings("resource")
	public static ArrayList<Recommendation> readRecommendationsFile() {
		ArrayList<Recommendation> recommendations = null;
		try {

			String[] array;
			File f = new File(System.getProperty("user.dir") + "/recommendations.csv");
			BufferedReader b = new BufferedReader(new FileReader(f));
			String readLine = "";
			while ((readLine = b.readLine()) != null) {

				array = readLine.split(COMMA_DELIMITER);

				if (recommendations == null) {
					recommendations = new ArrayList<Recommendation>();
				}

				ArrayList<BetterMethods> listBetterMethods = new ArrayList<BetterMethods>();
				
				String[] parameters;
				
				if(array.length == 17){
					parameters = new String[0];
				}
				
				else {
					parameters = new String[array.length - 17];
					int j=0;
					for(int i=16; i <array.length-1; i++){
						parameters[j] = array[i];
						j++;
					}
				}
				

				String[] betterMethods = array[array.length-1].split(SPECIAL_SEPARATOR);

				for (String betterMethod : betterMethods) {
					String[] betterMethodData = betterMethod.split(SPECIAL_DELIMITER);
					listBetterMethods.add(new BetterMethods(betterMethodData[0], betterMethodData[1],
							Double.parseDouble(betterMethodData[2]),
							new double[] { Double.parseDouble(betterMethodData[3]),
									Double.parseDouble(betterMethodData[4]), Double.parseDouble(betterMethodData[5]),
									Double.parseDouble(betterMethodData[6]), Double.parseDouble(betterMethodData[7]),
									Double.parseDouble(betterMethodData[8]) }));

				}

				recommendations.add(new Recommendation(Integer.parseInt(array[0]), array[1], array[2],parameters,
						new double[] { Double.parseDouble(array[3]), Double.parseDouble(array[4]),
								Double.parseDouble(array[5]), Double.parseDouble(array[6]),
								Double.parseDouble(array[7]), Double.parseDouble(array[8]) },
						new double[] { Double.parseDouble(array[9]), Double.parseDouble(array[10]),
								Double.parseDouble(array[11]), Double.parseDouble(array[12]),
								Double.parseDouble(array[13]), Double.parseDouble(array[14]) },
						Double.parseDouble(array[15]), listBetterMethods));

			}

			f.delete();

			return recommendations;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return recommendations;
		} catch (IOException e) {
			e.printStackTrace();
			return recommendations;
		}
	}
}
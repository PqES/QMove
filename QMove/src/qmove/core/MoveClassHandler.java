package qmove.core;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import qmove.moveclass.MCRecommendation;
import qmove.moveclass.MoveClass;
import qmove.utils.HandlerUtils;
import qmove.utils.SingletonNullProgressMonitor;

public class MoveClassHandler extends AbstractHandler {
	
	public static ArrayList<MCRecommendation> recommendations = new ArrayList<MCRecommendation>(); 

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Pega o projeto selecionado no project explorer
		IJavaProject projectOriginal = HandlerUtils.getProjectFromWorkspace(event);

		// Faz uma copia do projeto
		System.out.print("Copiando projeto " + projectOriginal.getElementName() + "... ");
		IJavaProject projectCopy = HandlerUtils.cloneProject(projectOriginal.getProject());
		System.out.println("OK");

		// Calcula as metricas do projeto
		System.out.println("Metricas atuais do projeto: ");
		double[] currentMetrics = HandlerUtils.calculateMetrics(projectCopy.getPrimaryElement());

		// Pega todas as classes do projeto
		ArrayList<IType> allClasses = HandlerUtils.getAllClasses(projectCopy.getProject());

		// Pega todos os pacotes do projeto
		ArrayList<IPackageFragment> allPackages = new ArrayList<IPackageFragment>();
		for (int i = 0; i < allClasses.size(); i++) {
			if (!allPackages.contains(allClasses.get(i).getPackageFragment())) {
				allPackages.add(allClasses.get(i).getPackageFragment());
			}
		}

		// Se tem somente um pacote, encerra execucao
		if (allPackages.size() <= 1) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information",
					"The selected project has only one package, so is not possible make Move Class refactorings");
			return null;
		}

		// Instancia classe qeu fara as refatoracoes move class 
		MoveClass mc = new MoveClass();
		
		boolean hadIncrease = true;
		
		while(hadIncrease != false && allClasses.size() != 0){
		
			double[] newMetrics;
			ArrayList<MCRecommendation> candidates = null;
	
			for (IType clazz : allClasses) {
	
				System.out.println("Classe em analise: " + clazz.getFullyQualifiedName());
				ArrayList<MCRecommendation> packageCandidates = null;
	
				for (IPackageFragment pckg : allPackages) {
	
					if (clazz.getPackageFragment().getElementName().compareTo(pckg.getElementName()) == 0) {
						continue;
					}
	
					System.out.print("Movendo classe para " + pckg.getElementName() + "... ");
					mc.performMoveClassRefactoring(clazz, pckg);
					System.out.println("OK");
	
					System.out.println("Novos valores das metricas:");
					newMetrics = HandlerUtils.calculateMetrics(projectCopy.getPrimaryElement());
	
					System.out.print("Voltando a classe para seu pacote original... ");
					mc.undoMoveClassRefactoring();
					System.out.println("OK");
	
					System.out.println("Recalculando metricas:");
					HandlerUtils.calculateMetrics(projectCopy.getPrimaryElement());
	
					double increase = HandlerUtils.compareMetrics(currentMetrics, newMetrics);
					if (increase > 0) {
						System.out.println("Metricas melhoram em " + increase + "%");
						if(packageCandidates == null){
							packageCandidates = new ArrayList<MCRecommendation>();
						}
						packageCandidates.add(new MCRecommendation(clazz, pckg, increase));
					
					} else if (increase == 0) {
						System.out.println("Metricas nao se alteram");
					
					} else {
						System.out.println("Metricas pioram em " + increase + "%");
					}
	
				}
				
				// Se nao houve nenhuma refatoracao que melhorou as metricas, pula para proxima classe
				if(packageCandidates == null){
					continue;
				}
				
				// Acha a melhor refatoracao para um determinado pacote
				MCRecommendation bestPackage = HandlerUtils.maxMetrics(packageCandidates);
	
				// Adiciona a melhor refatoracao em uma lista de candidatos a melhor refatoracao
				if(candidates == null){
					candidates = new ArrayList<MCRecommendation>();
				}
				
				candidates.add(bestPackage);
			}
			
			// Se nao houve nenhuma refatoracao que melhorasse as metricas, encerra a execucao
			if(candidates == null){
				hadIncrease = false;
				continue;
			}
			
			// Acha a melhor refatoracao dentre todas que melhoraram
			MCRecommendation bestRefactoring = HandlerUtils.maxMetrics(candidates);
			
			// Aplica a melhor refatoracao encontrada
			mc.performMoveClassRefactoring(bestRefactoring.getClazz(), bestRefactoring.getTarget());
			
			// Os valores das metricas atuais passam a ser os valores da melhor refatoracao
			currentMetrics = HandlerUtils.calculateMetrics(projectCopy.getPrimaryElement());
			
			// Retira a classe movida da lista de classes a serem analisadas
			allClasses.remove(bestRefactoring.getClazz());
			
			// Adiciona a melhor refatoracao na lista de recomendacoes
			recommendations.add(bestRefactoring);
			
			
		}
		
		System.out.println();
		System.out.println("RESULTADO: "+recommendations.size()+" recomendacoes");
		
		//Deleta a copia do projeto
		try {
			projectCopy.getProject().delete(true, SingletonNullProgressMonitor.getNullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

}

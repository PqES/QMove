package qmove.movemethod;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.builder.MetricsBuilder;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import net.sourceforge.metrics.ui.MetricsView;
import qmove.core.QMoveHandler;
import qmove.utils.SingletonNullProgressMonitor;
import qmove.utils.qMooveUtils;

@SuppressWarnings("restriction")
public class MoveMethod {
	
	
		
	private ArrayList<MethodMetric> potentialFiltred = new ArrayList<MethodMetric>();
	private MethodMetric candidateChosen;
	private double[] metricsOriginal;
	private IJavaElement je;
	public static double[] metricsModified;
	
	public MoveMethod(IJavaElement je){
		this.je = je;
		
	}
	
	
	public boolean ckeckIfMethodCanBeMoved(ClassMethod method) throws OperationCanceledException, CoreException{
		
		MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method.getMethod(),
				JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));

		processor.checkInitialConditions(new NullProgressMonitor());
		
		IVariableBinding[] potential = processor.getPossibleTargets();
        
		if(potential.length == 0 || potential == null) return false;
		
		else {
			
			for(int i=0; i<potential.length; i++){
				
				MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method.getMethod(),
						JavaPreferencesSettings.getCodeGenerationSettings(method.getMethod().getJavaProject()));
				
				processor2.setTarget(potential[i]);
				processor2.setInlineDelegator(true);
				processor2.setRemoveDelegator(true);
				processor2.setDeprecateDelegates(false);
	
				Refactoring refactoring = new MoveRefactoring(processor2);
				RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
				
				if(!status.isOK()) return false;
				
				else{
					Change undoChange = refactoring.createChange(new NullProgressMonitor());
					if(undoChange == null) return false;
				}
				
				processor2 = null;
			}
		}

		return true; 
		
	}
	
	

	
	public MethodsChosen startRefactoring(ClassMethod method, double[] metricsOriginal){
		
		this.metricsOriginal = metricsOriginal;
		
		MethodsChosen methodMoved = null;
		
			try {
				if(canMove(method.getMethod()))
					methodMoved = new MethodsChosen(method, candidateChosen.getPotential(), candidateChosen.getMetrics());
			} catch (OperationCanceledException | CoreException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return methodMoved;
	}
	
	public boolean canMove(IMethod method) throws OperationCanceledException, CoreException, InterruptedException {
		
		try{
			MoveInstanceMethodProcessor processor = null;
			
			while(processor == null){
			processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));
			}
			
			processor.checkInitialConditions(new NullProgressMonitor());
			
			IVariableBinding[] potential = processor.getPossibleTargets();
	        
			if(potential.length == 0 || potential == null) return false;
			
			System.out.println("-----------Metodo: "+method.getElementName()+"----------------" );
			
			IVariableBinding candidate;
			
			for (int i = 0; i < potential.length; i++) {
				
				
				MoveInstanceMethodProcessor processor2 = new MoveInstanceMethodProcessor(method,
						JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));
	
				processor2.checkInitialConditions(new NullProgressMonitor());
				
			
				candidate = potential[i];
				
				System.out.println("Calculando refactoring para "+candidate.getJavaElement().getPrimaryElement().getElementName());
				
				processor2.setTarget(candidate);
				processor2.setInlineDelegator(true);
				processor2.setRemoveDelegator(true);
				processor2.setDeprecateDelegates(false);
	
				Refactoring refactoring = new MoveRefactoring(processor2);
				refactoring.checkInitialConditions(new NullProgressMonitor());
				//RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
				//if(!status.isOK()) return false;
			
				//RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
				//if (status.getSeverity() != RefactoringStatus.OK) return false;
				//TODO Verificar o porque do status nao estar OK 
				
				final CreateChangeOperation create = new CreateChangeOperation(new CheckConditionsOperation(refactoring,
							CheckConditionsOperation.ALL_CONDITIONS),
							RefactoringStatus.FATAL);
				
				PerformChangeOperation perform = new PerformChangeOperation(create);
				
				MoveThread move = new MoveThread(perform);
		        move.start();
			
		        synchronized(move){
		              try{
		                  System.out.print("Aguardando o metodo ser movido... ");
		                  move.wait();
		              }catch(InterruptedException e){
		                  e.printStackTrace();
		              }
		   
		              System.out.println("Pronto!");
		        }
		        
		      System.out.print("Recalculando metricas... ");
		      
		        QMoveHandler.queueIsZero = false;
				while(QMoveHandler.queueIsZero == false){
					Thread.sleep(100);
				}
	        
				System.out.println("Pronto!");
				
		        
		        //Thread.sleep(10000);
		        
		     
		        
			
			//IWorkspace workspace = ResourcesPlugin.getWorkspace();	
			//workspace.run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			//ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			//workspace.run(perform, new NullProgressMonitor()); //move o metodo para calcular metricas
			//IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
			//workspace.run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			//Job.getJobManager().join(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			//Thread.sleep(1000);
			//workspace.join(ResourcesPlugin.PT_MOVE_DELETE_HOOK, SingletonNullProgressMonitor.getNullProgressMonitor());
			//ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD,SingletonNullProgressMonitor.getNullProgressMonitor());
			//Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, SingletonNullProgressMonitor.getNullProgressMonitor());
			//je.getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, SingletonNullProgressMonitor.getNullProgressMonitor());
			//Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, SingletonNullProgressMonitor.getNullProgressMonitor());
			
	        /*RebuildThread rebuild = new RebuildThread(je);
	        rebuild.start();
	        
	        synchronized(rebuild){
	              try{
	                  System.out.print("Reconstruindo o codigo-fonte... ");
	                  rebuild.wait();
	              }catch(InterruptedException e){
	                  e.printStackTrace();
	              }
	   
	              System.out.println("Pronto!");
	        }*/
	        
	        //while(MetricsBuilder.threadIsNull()){
	        //	Thread.sleep(1000);
	        //}
	        

	       
	        
	       /* synchronized(MetricsBuilder.getCalculatorThread()){
	              try{
	                  System.out.print("Calculando metricas... ");
	                  MetricsBuilder.getCalculatorThread().wait();
	              }catch(InterruptedException e){
	                  e.printStackTrace();
	              }
	   
	              System.out.println("Pronto!");
	        }*/
	        
	       /* CalculateMetricsThread calculate = new CalculateMetricsThread(je);
	        calculate.start();
	        
	        synchronized(calculate){
	              try{
	                  System.out.print("Calculando metricas... ");
	                  calculate.wait();
	              }catch(InterruptedException e){
	                  e.printStackTrace();
	              }
	   
	              System.out.println("Pronto!");
	        }*/
	        
			//metricsModified = Dispatcher.calculateAbstractMetricSource(je.getJavaProject()).getQmoodVariables();
			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
	        metricsModified = QMoodMetrics.getMetrics(ms);
	        qMooveUtils.writeCsvFile(method.getCompilationUnit().getParent().getElementName()+"."+method.getDeclaringType().getElementName()+":"+method.getElementName()+" / "+candidate.getType().getPackage().getName()+"."+candidate.getName(), metricsModified);
	        /*for(int k=0;k<metricsModified.length;k++)
				System.out.print(metricsModified[k]+" ");
			System.out.println();*/

	        //System.out.print("Desfazendo move method... ");
	        
			Change undoChange = perform.getUndoChange();
			//undoChange.perform(SingletonNullProgressMonitor.getNullProgressMonitor());
	        //Change undoChange = refactoring.createChange(null);
	        //PerformChangeOperation perform2 = new PerformChangeOperation(undoChange);
	        //ResourcesPlugin.getWorkspace().run(perform2, SingletonNullProgressMonitor.getNullProgressMonitor());
	        
			//System.out.println("Pronto!");
			
			UndoMoveThread undo = new UndoMoveThread(undoChange);
	        undo.start();
	        
	        synchronized(undo){
	              try{
	                  System.out.print("Desfazendo move method... ");
	                  undo.wait();
	              }catch(InterruptedException e){
	                  e.printStackTrace();
	              }
	   
	              System.out.println("Pronto!");
	        }
	        
	        System.out.print("Recalculando metricas... ");	
	        
	        QMoveHandler.queueIsZero = false;
			while(QMoveHandler.queueIsZero == false){
				Thread.sleep(100);
			}
			
			System.out.println("Pronto!");
	        
	        //Thread.sleep(1000);
	        
	        /*RebuildThread rebuild2 = new RebuildThread(je);
			rebuild2.start();
	        
	        synchronized(rebuild2){
	              try{
	                  System.out.print("Reconstruindo o codigo-fonte... ");
	                  rebuild2.wait();
	              }catch(InterruptedException e){
	                  e.printStackTrace();
	              }
	   
	              System.out.println("Pronto!");
	        }*/
			
			//undoChange.perform(new NullProgressMonitor());
			//IProgressMonitor nullProgressMonitor2 = new NullProgressMonitor();
			//Job.getJobManager().join(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			//Job.getJobManager().join(ResourcesPlugin.PT_MOVE_DELETE_HOOK, SingletonNullProgressMonitor.getNullProgressMonitor());
			//Thread.sleep(1000);
			//je.getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, SingletonNullProgressMonitor.getNullProgressMonitor());
			//Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, SingletonNullProgressMonitor.getNullProgressMonitor());
			
	        //Thread.sleep(1000);
			
			if(!checkIfSomeMetricDecrease(metricsModified))	{
				System.out.println("Nenhuma métrica piora");
				if(checkIfSomeMetricIncrease(metricsModified)){
					System.out.println("Pelo menos uma métrica melhora");
					potentialFiltred.add(new MethodMetric(potential[i], metricsModified));
					continue;
				}
				else System.out.println("Nenhuma métrica melhora");
			}
			else {
				System.out.println("Alguma métrica piora");
				return false;
			}
			
			
		}
		
		} catch(NullPointerException n){
			System.out.println(n.getMessage());
		}
		
		if(choosePotential()){
			potentialFiltred.clear();
			return true;
		}
		
		else return false;
			
		
	}
	
	public boolean checkIfSomeMetricDecrease(double[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i] - metricsOriginal[i];
			if(aux < 0) {
				return true;
			}
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) < 0) return true;
		}
		
		return false;
	}
	
	public boolean checkIfSomeMetricIncrease(double[] metricsModified){
		
		double aux;
		for(int i=0; i < metricsOriginal.length; i++){
			aux = metricsModified[i] - metricsOriginal[i];
			if(aux > 0) {
				return true;
			}
			//if((metricsModified[i].getValue() - metricsOriginal[i].getValue()) > 0) return true;
		}
		
		return false;
	}
	
	
	
	public boolean choosePotential() throws OperationCanceledException, CoreException{
		
		if(potentialFiltred.size() == 0 || potentialFiltred == null) return false;
		
		if(potentialFiltred.size() == 1) {
			candidateChosen = potentialFiltred.get(0);
			return true;
		}
		
		else {
	    
			candidateChosen = potentialFiltred.get(0);
			
			for(int i=1; i<potentialFiltred.size(); i++){
			
				if(potentialFiltred.get(i).getIncreasedMetricsSum(metricsOriginal)
						> candidateChosen.getIncreasedMetricsSum(metricsOriginal))
				candidateChosen = potentialFiltred.get(i);	
			}
				
				
			return true;
		}
		
	}
	
	
	
	/*public void calculeMetrics(AbstractMetricSource ms){
		
		double[] metrics = QMoodMetrics.getMetrics(ms);
		
		System.out.print("REU: "+metrics[4]);
		System.out.print(" FLE: "+metrics[2]);
		System.out.print(" EFE: "+metrics[0]);
		System.out.print(" EXT: "+metrics[1]);
		System.out.print(" FUN: "+metrics[3]);
		System.out.println(" UND: "+metrics[5]);
		
	}*/
		
}

class MoveThread extends Thread {
	
	private PerformChangeOperation perform;
	
    public MoveThread(PerformChangeOperation perform){
    	this.perform = perform;
    }
    
     @Override
     public void run(){
         synchronized(this){
        	 try {
				ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
             notify();
         }
     }
}

/*class RebuildThread extends Thread {
	
	private IJavaElement je;
	
    public RebuildThread(IJavaElement je){
    	this.je = je;
    }
    
     @Override
     public void run(){
         synchronized(this){
        	 try {
        		 je.getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, SingletonNullProgressMonitor.getNullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
             notify();
         }
     }
}*/

/*class CalculateMetricsThread extends Thread {
	
	private IJavaElement je;
	
    public CalculateMetricsThread(IJavaElement je){
    	this.je = je;
    }
    
     @Override
     public void run(){
         synchronized(this){
        	 MoveMethod.metricsModified = Dispatcher.calculateAbstractMetricSource(je.getJavaProject()).getQmoodVariables();
             notify();
         }
     }
}*/

class UndoMoveThread extends Thread {
	
	private Change undoChange;
	
    public UndoMoveThread(Change undoChange){
    	this.undoChange = undoChange;
    }
    
     @Override
     public void run(){
         synchronized(this){
        	 try {
        		 undoChange.perform(SingletonNullProgressMonitor.getNullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
             notify();
         }
     }
}
package net.sourceforge.metrics.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;
import net.sourceforge.metrics.ui.QMoodMetrics;
import qmove.movemethod.MoveMethod;
import qmove.utils.qMooveUtils;


public class QMoodHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		TreeSelection selection = (TreeSelection)HandlerUtil.getCurrentSelection(event);
	    IJavaElement je = (IJavaElement) selection.getFirstElement();
	    AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(je);
		
	    Metric[] metricsOriginal = QMoodMetrics.getQMoodMetrics(ms);
		
		try {
			getAllMethods(je,ms, event, metricsOriginal);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void getAllMethods(IJavaElement je, AbstractMetricSource ms, ExecutionEvent event, Metric[] metricsOriginal) throws CoreException{
		
		IJavaProject jproj = je.getJavaProject();
	    IProject p = (IProject)jproj.getResource();
	    
	    if(!p.isOpen()){
	    	p.open(null);
	    }
	    
	    Map<String, ArrayList<IMethod>> mapMethods = new HashMap<String, ArrayList<IMethod>>();
	    
	    mapMethods = qMooveUtils.getClassesMethods(p);
		
	    MoveMethod checkMove = new MoveMethod();
	 
	    
	    for (Map.Entry<String, ArrayList<IMethod>> entrada : mapMethods.entrySet()) {
	    	
	       	System.out.println(entrada.getKey());
	       
	       	   	for(int i=0; i<entrada.getValue().size(); i++){
	       		System.out.println(entrada.getValue().get(i));
	       		checkMove.startRefactoring(entrada.getValue().get(i), ms, event, metricsOriginal);
	       	}
		}
	}
}

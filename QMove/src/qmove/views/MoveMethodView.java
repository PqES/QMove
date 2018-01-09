package qmove.views;


import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.*;

import java.awt.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.texteditor.ITextEditor;



import java.awt.Dimension;
import javax.swing.JScrollPane;

import qmove.handlers.MoveMethodHandler;
import qmove.persistences.Recommendation;



public class MoveMethodView extends ViewPart{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "qmove.views.QMoveView";
	//IProgressMonitor m = new NullProgressMonitor();
    //IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	//IProject clone = workspaceRoot.getProject("Teste");
	//IMethod result = null;
	private Action doubleClickAction;
	private TableViewer viewer;
	private Action applyRefactoringAction;
	

	public MoveMethodView(){
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	
	
	
	public void createPartControl(Composite parent){
        GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);
        makeActions();
        createViewer(parent);
        //makeActions();
        hookDoubleClickAction();
        contributeToActionBars();
        
	}

	private void createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        createColumns(parent, viewer);
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());
        
        
        // get the content for the viewer, setInput will call getElements in the
        // contentProvider
        viewer.setInput(MoveMethodHandler.listRecommendations);
        // make the selection available to other views
        getSite().setSelectionProvider(viewer);
        // set the sorter for the table

        // define layout for the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
        applyRefactoringAction.setEnabled(true);
	}

	public TableViewer getViewer() {
        return viewer;
	}

	// create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
        String[] titles = { "ID", "Method", "To", "Increase" ,"Apply", "More Info"};
        int[] bounds = { 50, 100, 100, 100, 60, 80};
    

     // first column is for qmove id
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
        col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                	Recommendation m = (Recommendation) element;
                    return Integer.toString(m.getQMoveID());   
                }
        });

        
        
        // second column is for the method
        col = createTableViewerColumn(titles[1], bounds[1], 1);
        col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                	Recommendation m = (Recommendation) element;
                        return m.getMethod();
                }
        });

        // third column is for the class destiny
        col = createTableViewerColumn(titles[2], bounds[2], 2);
        col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                	Recommendation m = (Recommendation) element;
                    return m.getTarget();
                }
        });

        // apply refactoring button
        col = createTableViewerColumn(titles[3], bounds[3], 3);
        col.setLabelProvider(new ColumnLabelProvider() {
    
        	@Override
            public void update(ViewerCell cell) {

                TableItem item = (TableItem) cell.getItem();
                Button button = new Button((Composite) cell.getViewerRow().getControl(),SWT.PUSH | SWT.CENTER);
                button.setText(">");
                button.pack();
                button.addSelectionListener(new SelectionListener() {

                    public void widgetSelected(SelectionEvent event) {
                    	
                    }
                    
                    public void widgetDefaultSelected(SelectionEvent event) {
                        
                      }

                    
                });
                
                TableEditor editor = new TableEditor(item.getParent());
                editor.grabHorizontal  = true;
                editor.grabVertical = true;
                editor.setEditor(button , item, cell.getColumnIndex());
                editor.layout();
                
            }
        	
        });
        
        // more info button
        col = createTableViewerColumn(titles[4], bounds[4], 4);
        col.setLabelProvider(new ColumnLabelProvider() {
    
        	@Override
            public void update(ViewerCell cell) {

                TableItem item = (TableItem) cell.getItem();
                Button button = new Button((Composite) cell.getViewerRow().getControl(),SWT.PUSH | SWT.CENTER);
                button.setText("i");
                button.pack();
                button.addSelectionListener(new SelectionListener() {

                    public void widgetSelected(SelectionEvent event) {
                    	//System.out.println(item.getText(0));
                    	new GuiPrincipal(item.getText(0), MoveMethodHandler.listRecommendations).setVisible(true);
                    }
                    
                    public void widgetDefaultSelected(SelectionEvent event) {
                        
                      }

                    
                });
                
                TableEditor editor = new TableEditor(item.getParent());
                editor.grabHorizontal  = true;
                editor.grabVertical = true;
                editor.setEditor(button , item, cell.getColumnIndex());
                editor.layout();
                	
                };
            
        	
        });
        
        viewer.refresh();

	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	
		doubleClickAction = new Action() {
			public void run() {
				

			}
		};
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(applyRefactoringAction);
	
	}
	
	private void makeActions(){
		applyRefactoringAction = new Action() {
			public void run() {				
    			
			}
				
		};
		
		applyRefactoringAction.setToolTipText("Apply All Refactorings");
		applyRefactoringAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		applyRefactoringAction.setEnabled(false);
	}
	
}
	
	
class GuiPrincipal extends JFrame{
	     
    //variaveis para uso da JTable 
    private JTable table;
    private final String colunas[] ={"Method/Target","REU","FLE", "EFE", "EXT", "FUN", "ENT", "Average"};
    private String dados[][];
     
        /*Construtor da classe ,
          antes de executar o metodo main(),
          irá construir o JFrame e a JTable*/
    public GuiPrincipal(String qmoveID, ArrayList<Recommendation> listRecommendations) {
    	int id = Integer.parseInt(qmoveID);
    	for(int i=0; i < listRecommendations.size(); i++){
    		if(id == listRecommendations.get(i).getQMoveID()){
    			dados = listRecommendations.get(i).getMethodsTable().getMethodsMetrics();
    		}
    	}
        setLayout(new FlowLayout());//tipo de layout
        setSize(new Dimension(600, 200));//tamanho do Formulario
        setLocationRelativeTo(null);//centralizado
        setTitle("More Info");//titulo
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//setando a ação padrão de fechamento do Formulário,
                                                               // neste caso  irá fechar o programa
         
                //instanciando a JTable
        table=new JTable(dados,colunas);
        table.setPreferredScrollableViewportSize(new Dimension(500,100));//barra de rolagem
        table.setFillsViewportHeight(true);
         
                //adicionando a tabela em uma barra de rolagem, ficará envolta , pela mesma 
        JScrollPane scrollPane=new JScrollPane(table);
                 
                //adicionando ao JFrame "Formulário" a JTable "Tabela" 
        add(scrollPane);
    }
     
}


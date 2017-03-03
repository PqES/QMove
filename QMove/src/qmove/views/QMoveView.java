package qmove.views;


import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;

import qmove.movemethod.Recommendation;


public class QMoveView extends ViewPart{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "qmove.views.QMoveView";

	private TableViewer viewer;
	
	

/**
	 * The constructor.
	 */
	public QMoveView(){
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	
	
	
	public void createPartControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);
        createViewer(parent);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                    viewer.refresh();
                    
            }
    });
        
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
        viewer.setInput(qmove.core.QMoveHandler.listRecommendations);
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
	}

	public TableViewer getViewer() {
        return viewer;
	}

	// create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
        String[] titles = { "Method", "To", "Increase" ,"Apply", "More Info"};
        int[] bounds = { 100, 100, 100, 60, 80};

        // first column is for the method
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
        col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                	Recommendation m = (Recommendation) element;
                        return m.getPackageMethodName()+"."+m.getClassMethodName()+"::"+m.getMethodName();
                }
        });

        // second column is for the class origin
        col = createTableViewerColumn(titles[1], bounds[1], 1);
        col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                	Recommendation m = (Recommendation) element;
                    return m.getPackageTargetName()+"."+m.getClassTargetName();
                }
        });

        // now the class destiny
        col = createTableViewerColumn(titles[2], bounds[2], 2);
        col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                	Recommendation m = (Recommendation) element;
                	String increase = String.format("%.2f",m.getIncrease());//String.valueOf(m.getIncrease());
                    return increase+"%";
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
                TableEditor editor = new TableEditor(item.getParent());
                editor.grabHorizontal  = true;
                editor.grabVertical = true;
                editor.setEditor(button , item, cell.getColumnIndex());
                editor.layout();
            }
        	
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
}

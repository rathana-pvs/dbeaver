package org.jkiss.dbeaver.ext.cubrid.ui.controls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCStatistics;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UIStyles;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CustomSashForm;
import org.jkiss.dbeaver.ui.controls.resultset.AbstractPresentation;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetController;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetCopySettings;


public class CubridInfoStistic extends AbstractPresentation {
	private static final Log log = Log.getLog(CubridInfoStistic.class);
    private Table table;
    private DBDAttributeBinding curAttribute;
    private Composite control;
	private SashForm planPanel;
	private Text plainText;

    @Override
    public void createPresentation(@NotNull IResultSetController controller, @NotNull Composite parent) {
        super.createPresentation(controller, parent);
        
      control = UIUtils.createPlaceholder(parent, 1);
      control.setLayoutData(new GridData(GridData.FILL_BOTH));

      

      
      this.planPanel = new CustomSashForm(control, SWT.VERTICAL);
      this.planPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
//      this.planPanel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
      final GridLayout gl = new GridLayout(1, false);
      gl.marginWidth = 0;
      gl.marginHeight = 0;
      this.planPanel.setLayout(gl);
        
        table = new Table(planPanel, SWT.MULTI | SWT.FULL_SELECTION);
        table.setLinesVisible(!UIStyles.isDarkTheme());
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                curAttribute = null;
                TableItem[] selection = table.getSelection();
                Object[] elements = new Object[selection.length];
                for (int i = 0; i < selection.length; i++) {
                    elements[i] = selection[i].getData();
                    if (curAttribute == null) {
                        curAttribute = (DBDAttributeBinding) elements[i];
                    }
                }
                fireSelectionChanged(new StructuredSelection(elements));
            }
        });

        UIUtils.createTableColumn(table, SWT.LEFT, "Name");
        UIUtils.createTableColumn(table, SWT.LEFT, "Value");

//        UIUtils.setControlContextMenu(table, manager -> UIUtils.fillDefaultTableContextMenu(manager, table));
        
        plainText = new Text(this.planPanel, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
    	
    }

    @Override
    public Control getControl() {
        return table;
    }

    @Override
    public void refreshData(boolean refreshMetadata, boolean append, boolean keepState) {
        
        DBCStatistics st = controller.getModel().getStatistics();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        
        if(st != null) {
        	table.removeAll();
        	this.readStatistic(st.getQueryText());
        	TableItem queryText = new TableItem(table, SWT.LEFT);
        	TableItem updatedRow = new TableItem(table, SWT.LEFT);
        	TableItem executeTime = new TableItem(table, SWT.LEFT);
        	TableItem fetchTime = new TableItem(table, SWT.LEFT);
        	TableItem totalTime = new TableItem(table, SWT.LEFT);
        	TableItem startTime = new TableItem(table, SWT.LEFT);
        	TableItem finishTime = new TableItem(table, SWT.LEFT);

        	
        	queryText.setText(0, "Query Text");
        	queryText.setText(1, st.getQueryText());
        	
        	updatedRow.setText(0, "Updated Row");
        	updatedRow.setText(1, String.valueOf(st.getRowsUpdated()));
        	
        	executeTime.setText(0, "Execute Time");
        	executeTime.setText(1, String.format("%ss", Double.valueOf(st.getExecuteTime())/1000));
        	
        	fetchTime.setText(0, "Fetch Time");
        	fetchTime.setText(1, String.format("%ss", Double.valueOf(st.getFetchTime())/1000));       	
        	
        	totalTime.setText(0, "Total Time");
        	totalTime.setText(1, String.format("%ss", Double.valueOf(st.getTotalTime())/1000));
        	
        	startTime.setText(0, "Start Time");
        	startTime.setText(1, String.valueOf(df.format(new Date(st.getStartTime()))));
        	
        	finishTime.setText(0, "Finish Time");
        	finishTime.setText(1, String.valueOf(df.format(new Date(st.getEndTime()))));
        	
        }

        UIUtils.packColumns(table);
        
        
    }

    @Override
    public void formatData(boolean refreshData) {

    }

    @Override
    public void clearMetaData() {

    }

    @Override
    public void updateValueView() {

    }

    @Override
    public void changeMode(boolean recordMode) {

    }

    @Nullable
    @Override
    public DBDAttributeBinding getCurrentAttribute() {
        return curAttribute;
    }

    @NotNull
    @Override
    public Map<Transfer, Object> copySelection(ResultSetCopySettings settings) {
        return Collections.emptyMap();
    }
    
    private void readStatistic(String query) {
    	 new AbstractJob("Read Statistic")
         {
    		 @Override
    			protected IStatus run(DBRProgressMonitor monitor) {
    			
    			 UIUtils.syncExec(
                         () -> {
                        	 try (JDBCSession session = DBUtils.openMetaSession(monitor, controller.getDataContainer().getDataSource(), "Read Statistic")) {
                    		 JDBCStatement stmn = session.createStatement();
                    		 stmn.execute(query);
                    		 JDBCResultSet dbResult = stmn.executeQuery("show trace");
 		                    if(dbResult.next()) {
 		                    	String st = dbResult.getString("trace");
 		                    	plainText.setText(st);
 		                    }
             		            
             		        } catch (SQLException | DBCException e) {
             		            log.error("could not read statistic", e);
             		        }
                         });
    				
    			 return Status.OK_STATUS;
    			}
    	    	
         }.schedule();
        
    }
    
}

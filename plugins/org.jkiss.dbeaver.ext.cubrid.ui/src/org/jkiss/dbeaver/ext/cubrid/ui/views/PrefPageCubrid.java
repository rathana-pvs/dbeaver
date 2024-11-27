package org.jkiss.dbeaver.ext.cubrid.ui.views;

import java.sql.SQLException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridSQLDialect;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.app.DBPPlatform;
import org.jkiss.dbeaver.model.connection.DBPDataSourceProviderDescriptor;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLDialectMetadata;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.preferences.PreferenceStoreDelegate;
import org.jkiss.dbeaver.ui.preferences.TargetPrefPage;
import org.jkiss.dbeaver.utils.PrefUtils;

public class PrefPageCubrid extends TargetPrefPage
{
	public static final String PAGE_ID = "org.jkiss.dbeaver.preferences.cubrid.general"; //$NON-NLS-1$
	private Button showStatistic;
	private Button info;
	private Button allInfo;
	 public PrefPageCubrid()
	    {
	        super();
	        setPreferenceStore(new PreferenceStoreDelegate(DBWorkbench.getPlatform().getPreferenceStore()));
	    }
	
	@Override
	protected boolean hasDataSourceSpecificOptions(DBPDataSourceContainer dsContainer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean supportsDataSourceSpecificOptions() {
		return true;
	}

	@Override
	protected void loadPreferences(DBPPreferenceStore store) {
		DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
		showStatistic.setSelection(preferenceStore.getBoolean("show"));
		info.setSelection(preferenceStore.getBoolean("info"));
		allInfo.setSelection(preferenceStore.getBoolean("allInfo"));
		
		
		
		
		
	}

	@Override
	protected void savePreferences(DBPPreferenceStore store) {
		
		DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
		preferenceStore.setValue("show", showStatistic.getSelection());
		preferenceStore.setValue("info", info.getSelection());
		preferenceStore.setValue("allInfo", allInfo.getSelection());
		
		PrefUtils.savePreferenceStore(store);
		
		this.enableTracking(showStatistic.getSelection());
		
	}

	@Override
	protected void clearPreferences(DBPPreferenceStore store) {
		
		DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
		preferenceStore.setValue("show", false);
		preferenceStore.setValue("info", false);
		preferenceStore.setValue("allInfo", false);
		
	}

	@Override
	protected String getPropertyPageID() {
		
		return PAGE_ID;
	}

	@Override
	protected Control createPreferenceContent(Composite parent) {
		Composite composite = UIUtils.createPlaceholder(parent, 1);
		GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        composite.setLayout(gl);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        {
            Group planGroup = UIUtils.createControlGroup(composite, "Query Editor", 1, GridData.FILL_HORIZONTAL, 0);
            showStatistic = UIUtils.createCheckbox(planGroup, "Show Statistics Info", true);
            allInfo = UIUtils.createCheckbox(planGroup, "Show Statistics ALL Info", false);
            info = UIUtils.createCheckbox(planGroup, "Show Trace Info", false);

        }
        
       
		return composite;
	}
	
	 private void enableTracking(boolean enabled) {
////		 SQLDialectMetadata d = DBWorkbench.getPlatform().getSQLDialectRegistry().getDialect("cubrid");
//		try {
//			CubridSQLDialect c =  (CubridSQLDialect) d.createInstance();
//			
//		} catch (DBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		 String sql = enabled ? "SET TRACE ON" : "OFF";
    	 new AbstractJob("Enable Tracking")
         {
    		 @Override
    			protected IStatus run(DBRProgressMonitor monitor) {
    			
    			 UIUtils.syncExec(
                         () -> {
                        	 try (JDBCSession session = DBUtils.openMetaSession(monitor, null, "Read Statistic")) {
                        		 JDBCPreparedStatement st = session.prepareStatement(sql);
                        		 st.execute();
                        		 
                 		        } catch (SQLException | DBCException e) {
                 		            
                 		        }
                             });
                        	 
                       
    			 return Status.OK_STATUS;
	 }
         }.schedule();
	 }
	 }
	 
    			 

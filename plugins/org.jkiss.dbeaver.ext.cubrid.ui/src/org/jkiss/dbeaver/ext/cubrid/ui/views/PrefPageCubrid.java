package org.jkiss.dbeaver.ext.cubrid.ui.views;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.jkiss.dbeaver.ext.cubrid.CubridConstants;
import org.jkiss.dbeaver.ext.cubrid.ui.internal.CubridMessages;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.preferences.PreferenceStoreDelegate;
import org.jkiss.dbeaver.ui.preferences.TargetPrefPage;
import org.jkiss.dbeaver.utils.PrefUtils;

public class PrefPageCubrid extends TargetPrefPage
{
	public static final String PAGE_ID = "org.jkiss.dbeaver.preferences.cubrid.general"; //$NON-NLS-1$
	private Button trace;
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
		trace.setSelection(store.getBoolean(CubridConstants.STATISTIC_TRACE));
		if(store.getString(CubridConstants.STATISTIC).equals(CubridConstants.STATISTIC_INFO)) {
			info.setSelection(true);
		}else if(store.getString(CubridConstants.STATISTIC).equals(CubridConstants.STATISTIC_ALL_INFO)) {
			allInfo.setSelection(true);
		}
	}

	@Override
	protected void savePreferences(DBPPreferenceStore store) {
		store.setValue(CubridConstants.STATISTIC_TRACE, trace.getSelection());
		PrefUtils.savePreferenceStore(store);
		
	}

	@Override
	protected void clearPreferences(DBPPreferenceStore store) {
		store.setValue(CubridConstants.STATISTIC_TRACE, false);
		store.setValue(CubridConstants.STATISTIC, "");
	}

	@Override
	protected String getPropertyPageID() {
		
		return PAGE_ID;
	}

	@Override
	protected Control createPreferenceContent(Composite parent) {
		DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
		Composite composite = UIUtils.createPlaceholder(parent, 1);
		GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        composite.setLayout(gl);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        {
        	
            Group planGroup = UIUtils.createControlGroup(composite, CubridMessages.statistic_group_editor_title, 1, GridData.FILL_HORIZONTAL, 0);
            info = UIUtils.createCheckbox(planGroup, CubridMessages.statistic_info, false);
            allInfo = UIUtils.createCheckbox(planGroup, CubridMessages.statistic_all_info, false);
            trace = UIUtils.createCheckbox(planGroup, CubridMessages.statistic_trace_info, false);
            
            info.addSelectionListener(new SelectionAdapter() {
            	@Override
				public void widgetSelected(SelectionEvent e) {
            		
            		if(info.getSelection()) {
            			allInfo.setSelection(false);
            			preferenceStore.setValue(CubridConstants.STATISTIC, CubridConstants.STATISTIC_INFO);
            		}else {
            			preferenceStore.setValue(CubridConstants.STATISTIC, "");
            		}
            	}
            });
            
            allInfo.addSelectionListener(new SelectionAdapter() {
            	@Override
				public void widgetSelected(SelectionEvent e) {
            		if(allInfo.getSelection()) {
            			info.setSelection(false);
            			preferenceStore.setValue(CubridConstants.STATISTIC, CubridConstants.STATISTIC_ALL_INFO);
            		}else {
            			preferenceStore.setValue(CubridConstants.STATISTIC, "");
            		}
            	}
            });

        }
        
       
		return composite;
	}
	
	 }
	 
    			 

package org.jkiss.dbeaver.ext.cubrid.ui.views;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.preferences.PreferenceStoreDelegate;
import org.jkiss.dbeaver.ui.preferences.TargetPrefPage;

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
		boolean isShow = preferenceStore.getBoolean("show");
		showStatistic.setEnabled(isShow);
		info.setEnabled(preferenceStore.getBoolean("info"));
		allInfo.setEnabled(preferenceStore.getBoolean("allInfo"));
		
		if(isShow) {
			
		}
		
		
	}

	@Override
	protected void savePreferences(DBPPreferenceStore store) {
		
		DBPPreferenceStore preferenceStore = DBWorkbench.getPlatform().getPreferenceStore();
		preferenceStore.setValue("show", showStatistic.getEnabled());
		preferenceStore.setValue("info", info.getEnabled());
		preferenceStore.setValue("allInfo", allInfo.getEnabled());
		
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
            showStatistic = UIUtils.createCheckbox(planGroup, "Show Statistics Info", false);
            allInfo = UIUtils.createCheckbox(planGroup, "Show Statistics ALL Info", false);
            info = UIUtils.createCheckbox(planGroup, "Show Trace Info", false);

        }
		return composite;
	}
}

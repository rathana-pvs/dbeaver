package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.ui.preferences.TargetPrefPage;

public class PrefStatistic extends TargetPrefPage{
	public static final String PAGE_ID = "org.jkiss.dbeaver.preferences.main.resultset.grid"; //$NON-NLS-1$

	@Override
	protected boolean hasDataSourceSpecificOptions(DBPDataSourceContainer dsContainer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean supportsDataSourceSpecificOptions() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void loadPreferences(DBPPreferenceStore store) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void savePreferences(DBPPreferenceStore store) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clearPreferences(DBPPreferenceStore store) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getPropertyPageID() {
		// TODO Auto-generated method stub
		return PAGE_ID;
	}

	@Override
	protected Control createPreferenceContent(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}
}

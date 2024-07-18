package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.ISmartTransactionManager;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.IRefreshablePart.RefreshResult;
import org.jkiss.dbeaver.ui.controls.ObjectEditorPageControl;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;
import org.jkiss.dbeaver.ui.editors.DatabaseEditorUtils;
import org.jkiss.dbeaver.ui.editors.IRevertableEditor;
public class CubridUserUI  extends AbstractDatabaseObjectEditor<CubridUser>{
    
    private CubridUser user;
    private Table table;
    
    @Override
    public RefreshResult refreshPart(Object source, boolean force) {
        // TODO Auto-generated method stub
        return RefreshResult.REFRESHED;
    }

    @Override
    public void createPartControl(Composite parent) {
        UserPageControl pageControl = new UserPageControl(parent, this);
        Composite container = UIUtils.createPlaceholder(pageControl, 2, 10);
        
        this.user = this.getDatabaseObject();
        GridData gds = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gds);
       
        

        
        {
            
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.heightHint = 30;
            Text t = UIUtils.createLabelText(container, "Name ", user.getName(), SWT.BORDER, new GridData(GridData.FILL_HORIZONTAL));
            GridData gd1 = new GridData();
            gd1.widthHint = 400;
            t.setLayoutData(gd1);
            
        }
        {
            
            Text t = UIUtils.createLabelText(container, "Password ","",  SWT.BORDER | SWT.PASSWORD);
            GridData gd1 = new GridData();
            gd1.widthHint = 400;
            t.setLayoutData(gd1);
        }
        {
            UIUtils.createControlLabel(container, "Groups", 1);
            table = new Table(container, SWT.BORDER | SWT.CHECK);
            GridData gd = new GridData();
            gd.heightHint = 100;
            gd.widthHint = 390;
            
            table.setLayoutData(gd);
        }
        {
          Text t = UIUtils.createLabelText(container, "Description", user.getDescription(), SWT.BORDER|SWT.WRAP|SWT.MULTI|SWT.V_SCROLL);
          GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
          gd1.heightHint = 3 * t.getLineHeight();
          gd1.widthHint = 400;
          
          t.setLayoutData(gd1);
          
      }
        
          loadGroups();
       pageControl.createProgressPanel();
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
        
    }
    
    private void loadGroups() {

        new AbstractJob("Load groups") {

            @Override
            protected IStatus run(DBRProgressMonitor monitor) {
                try {

                    List<GenericSchema> cubridUsers = user.getDataSource().getDataSource().getCubridUsers(monitor);
                    UIUtils.syncExec(
                            () -> {
                                table.removeAll();
                                for (GenericSchema group : cubridUsers) {
                                    TableItem item = new TableItem(table, SWT.BREAK);
                                    item.setImage(DBeaverIcons.getImage(DBIcon.TREE_USER_GROUP));
                                    item.setText(0, group.getName());
                                    item.setChecked(user.getRoles().contains(group.getName()));
                                }
                            });

                } catch (DBException e) {
                    DBWorkbench.getPlatformUI().showError("User List", "Can't read User list", e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    protected class UserPageControl extends ObjectEditorPageControl {

        public UserPageControl(Composite parent, CubridUserUI object) {
            super(parent,SWT.NONE, object);
        }
        
        @Override
        public void fillCustomActions(IContributionManager contributionManager) {
            super.fillCustomActions(contributionManager);

            contributionManager.add(new Separator());

            IWorkbenchSite workbenchSite = getSite();
            if (workbenchSite != null) {
                DatabaseEditorUtils.contributeStandardEditorActions(workbenchSite, contributionManager);
            }
        }
    }
    
    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

  
   
    
    
}

package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPEvent;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.edit.prop.DBECommandProperty;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ObjectEditorPageControl;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;
import org.jkiss.dbeaver.ui.editors.ControlPropertyCommandListener;
import org.jkiss.dbeaver.ui.editors.DatabaseEditorUtils;
import org.jkiss.utils.CommonUtils;
public class CubridUserUI  extends AbstractDatabaseObjectEditor<CubridUser>{
    
    private CubridUser user;
    private Table table;
    private List<String> groups = new ArrayList<>();
    UserPageControl pageControl;
    @Override
    public RefreshResult refreshPart(Object source, boolean force) {
        // TODO Auto-generated method stub
        this.refreshUser();
        return RefreshResult.REFRESHED;
    }

    @Override
    public void createPartControl(Composite parent) {
        pageControl = new UserPageControl(parent, this);
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
            if(this.getDatabaseObject().isPersisted()) {
               t.setEditable(false); 
            }
            ControlPropertyCommandListener.create(this, t, CubridUserHandler.NAME);
            
        }
        {
            
            Text t = UIUtils.createLabelText(container, "Password ","",  SWT.BORDER | SWT.PASSWORD);
            GridData gd1 = new GridData();
            gd1.widthHint = 400;
            t.setLayoutData(gd1);
            ControlPropertyCommandListener.create(this, t, CubridUserHandler.PASSWORD);
        }
        {
            
            UIUtils.createControlLabel(container, "Groups", 1);
            table = new Table(container, SWT.BORDER | SWT.CHECK);
            GridData gd = new GridData();
            gd.heightHint = 100;
            gd.widthHint = 390;
            
            table.setLayoutData(gd);
            loadGroups();
            new TableCommandListener(this, table,  CubridUserHandler.GROUPS, groups);
            if(this.getDatabaseObject().isPersisted()) {
                table.setEnabled(false); 
             }

            
        }
        {
          Text t = UIUtils.createLabelText(container, "Description", user.getDescription(), SWT.BORDER|SWT.WRAP|SWT.MULTI|SWT.V_SCROLL);
          GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
          gd1.heightHint = 3 * t.getLineHeight();
          gd1.widthHint = 400;
          
          t.setLayoutData(gd1);
          ControlPropertyCommandListener.create(this, t, CubridUserHandler.DESCRIPTION);
//          DBUtils.fireObjectUpdate(user);
         
      }
        
          
       pageControl.createProgressPanel();
       DBUtils.fireObjectUpdate(getDatabaseObject(), null, DBPEvent.RENAME);
       
    }

    @Override
    public void setFocus() {
        if (pageControl != null) {
            // Important! activation of page control fills action toolbar
            this.pageControl.activate(true);
        }
        
    }
    
    private void loadGroups() {
        
        new AbstractJob("Load groups") {

            @Override
            protected IStatus run(DBRProgressMonitor monitor) {
                List<GenericSchema> cubridUsers = user.getDataSource().getDataSource().getSchemas();
                UIUtils.syncExec(
                        () -> {
                            table.removeAll();
                            groups.clear();
                            for (GenericSchema group : cubridUsers) {
                                if(!group.getName().equals(user.getName())) {
                                    TableItem item = new TableItem(table, SWT.BREAK);
                                    item.setImage(DBeaverIcons.getImage(DBIcon.TREE_USER_GROUP));
                                    item.setText(0, group.getName());
                                    
                                    if(user.getRoles().contains(group.getName())) {
                                        groups.add(group.getName());
                                        item.setChecked(true);
                                        
                                    }
                                }
                               
                            }
                            
                        });
                return Status.OK_STATUS;
            }
        }.schedule();
    }
    
    private void refreshUser() {

        new AbstractJob("Load groups") {

            @Override
            protected IStatus run(DBRProgressMonitor monitor) {
                CubridDataSource dataSource = (CubridDataSource) user.getDataSource();
                try {
                    dataSource.refreshObject(monitor);
                } catch (DBException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
    
    private class TableCommandListener{
        final private CubridUserUI editor;
        final private Table widget;
        final private CubridUserHandler handler;
        private List<String> values;
        private DBECommandProperty<CubridUser> command;
        private List<String> oldValue;
        
        public TableCommandListener(CubridUserUI editor, Table widget, CubridUserHandler handler, List<String> oldValue){
            this.editor = editor;
            this.widget = widget;
            this.handler = handler;
            this.oldValue = oldValue;
            
            addEventListener();
            
            
        }
        
        private void addEventListener() {
            widget.addListener(
                    SWT.Selection,
                    event -> {
                        TableItem item = (TableItem) event.item;
                        if(values == null) {
                            values = new ArrayList<>(oldValue);
                        }
                        
                        if (item != null) {
                            
                            if (item.getChecked()) {
                                values.add(item.getText());
                            }else {
                                values.removeIf(value->value == item.getText());
                            }
                        }
                        
                        
                        DBECommandReflector<CubridUser, DBECommandProperty<CubridUser>> commandReflector = new DBECommandReflector<CubridUser, DBECommandProperty<CubridUser>>() {

                            @Override
                            public void redoCommand(DBECommandProperty<CubridUser> command) {
                                
                            }

                            @Override
                            public void undoCommand(DBECommandProperty<CubridUser> cp) {
                               
                               editor.loadGroups();
                               values = new ArrayList<String>(oldValue);
                           
                            }
                      
                        };
                        
                        if (command == null) {
                            if (!CommonUtils.equalObjects(values, oldValue)) {
                                command = new DBECommandProperty<CubridUser>(editor.getDatabaseObject(), handler, oldValue, values);
                                editor.addChangeCommand(command, commandReflector);
                            }
                        } else {
                            if (CommonUtils.equalObjects(values, oldValue)) {
                                editor.removeChangeCommand(command);
                                command = null;
                                
                            } else {
                                command.setNewValue(values);
                                editor.updateChangeCommand(command, commandReflector);
                            }
                        }
                    });
        }
        
    }

  
   
    
    
}

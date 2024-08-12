package org.jkiss.dbeaver.ext.cubrid.ui.editors;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.colorchooser.ColorSelectionModel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.ext.cubrid.model.CubridPrivilage;
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
public class CubridUserUI  extends AbstractDatabaseObjectEditor<CubridPrivilage>{
    
    private CubridPrivilage user;
    private Table table;
    private List<String> groups = new ArrayList<>();
    private boolean allowEditPassword = false;
    UserPageControl pageControl;
    @Override
    public RefreshResult refreshPart(Object source, boolean force) {

        return RefreshResult.REFRESHED;
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        super.init(site, input);

        
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
            t.setEditable(!this.getDatabaseObject().isPersisted());
            
            ControlPropertyCommandListener.create(this, t, CubridUserHandler.NAME);
            
        }
        {
            
            String loginedUser = user.getDataSource().getContainer().getConnectionConfiguration().getUserName().toUpperCase();
            allowEditPassword = new ArrayList<>(Arrays.asList("DBA", user.getName())).contains(loginedUser);
            
            Text t = UIUtils.createLabelText(container, "Password ","",  SWT.BORDER | SWT.PASSWORD);
            GridData gd1 = new GridData();
            gd1.widthHint = 400;
            t.setLayoutData(gd1);
            t.setEditable(allowEditPassword);
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
        
//        {
//            
//            UIUtils.createControlLabel(container, "Groups", 1);
//
//            final ScrolledComposite sc1 =  new ScrolledComposite(container, SWT.BORDER);
//            sc1.setLayout(new GridLayout(1, false));
//            
//            Composite wrapper = new Composite(sc1,SWT.BORDER);
//            GridData gd = new GridData(GridData.FILL_BOTH);
//            
//            wrapper.setLayoutData(gd);
//            UIUtils.configureScrolledComposite(sc1, wrapper);
//            
//            for(int i=0; i<10; i++) {
////                UIUtils.createCheckbox(c1, "Hello", true);
//                Button b1 = new Button (wrapper, SWT.PUSH);
//                b1.setText("first button");
//            }
//            sc1.setMinSize(390,  100);
//            
//            
//
//            
//        }
        {
          Text t = UIUtils.createLabelText(container, "Description", user.getDescription(), SWT.BORDER|SWT.WRAP|SWT.MULTI|SWT.V_SCROLL);
          GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
          gd1.heightHint = 3 * t.getLineHeight();
          gd1.widthHint = 400;
          
          t.setLayoutData(gd1);
          ControlPropertyCommandListener.create(this, t, CubridUserHandler.DESCRIPTION);

         
      }
        
          
       pageControl.createProgressPanel();
       
       
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
                List<CubridPrivilage> cubridUsers;
                try {
                    cubridUsers = user.getDataSource().getCubridPrivilages(monitor);
                
                    UIUtils.syncExec(
                            () -> {
                                table.removeAll();
                                groups.clear();
                                for (CubridPrivilage privilage : cubridUsers) {
                                    if(!privilage.getName().equals(user.getName())) {
                                        TableItem item = new TableItem(table, SWT.BREAK);
                                        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_USER_GROUP));
                                        item.setText(0, privilage.getName());
                                        
                                        if(user.getRoles().contains(privilage.getName())) {
                                            groups.add(privilage.getName());
                                            item.setChecked(true);
                                            
                                        }
                                    }
                                   
                                }
                            
                        });
                } catch (DBException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
        private DBECommandProperty<CubridPrivilage> command;
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
                        
                        
                        DBECommandReflector<CubridPrivilage, DBECommandProperty<CubridPrivilage>> commandReflector = new DBECommandReflector<CubridPrivilage, DBECommandProperty<CubridPrivilage>>() {

                            @Override
                            public void redoCommand(DBECommandProperty<CubridPrivilage> command) {
                                
                            }

                            @Override
                            public void undoCommand(DBECommandProperty<CubridPrivilage> cp) {
                               
                               editor.loadGroups();
                               values = new ArrayList<String>(oldValue);
                           
                            }
                      
                        };
                        
                        if (command == null) {
                            if (!CommonUtils.equalObjects(values, oldValue)) {
                                command = new DBECommandProperty<CubridPrivilage>(editor.getDatabaseObject(), handler, oldValue, values);
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

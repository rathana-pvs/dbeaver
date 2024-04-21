/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.cubrid.tools;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridTable;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.ext.generic.model.GenericTable;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeToolWizardPage;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.GridData;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.jkiss.dbeaver.ui.controls.CustomSashForm;

public class CubridExportWizardPageObjects
        extends AbstractNativeToolWizardPage<CubridExportWizard> {

    private Table tableTables;
    List<CubridTable> checkedTables = new ArrayList<>();

    protected CubridExportWizardPageObjects(CubridExportWizard wizard) {
        super(wizard, "Export");
        setTitle("Choose tables to export");
        setDescription("Choose tables to export");
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = UIUtils.createPlaceholder(parent, 1);

        Group objectsGroup =
                UIUtils.createControlGroup(composite, "Tables", 1, GridData.FILL_HORIZONTAL, 0);
        objectsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        SashForm sash = new CustomSashForm(objectsGroup, SWT.VERTICAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));

        {
            Composite catPanel = UIUtils.createComposite(sash, 1);
            catPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
            tableTables = new Table(catPanel, SWT.BORDER | SWT.CHECK);
            tableTables.addListener(
                    SWT.Selection,
                    event -> {
                        TableItem item = (TableItem) event.item;
                        if (item != null) {
                            CubridTable table = (CubridTable) item.getData();
                            if (event.detail == SWT.CHECK) {
                                checkedTables.remove(table);
                            }
                        }

                        updateState();
                    });
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 50;
            tableTables.setLayoutData(gd);

            Composite buttonsPanel = UIUtils.createComposite(catPanel, 3);
            buttonsPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            new Label(buttonsPanel, SWT.NONE).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
            createCheckButtons(buttonsPanel, tableTables);
        }
        loadTables();
        setControl(composite);
    }

    private void updateCheckedTables() {
        TableItem[] tableItems = tableTables.getItems();
        List<String> tables = new ArrayList<>();
        for (TableItem item : tableItems) {

            if (item.getChecked()) {
                checkedTables.add((CubridTable) item.getData());
                tables.add(item.getText());
            }
        }

        //        loadTables();
        this.wizard.getSettings().setTables(tables);
    }

    private void loadTables() {

        new AbstractJob("Load tables") {
            {
                setUser(true);
            }

            @Override
            protected IStatus run(DBRProgressMonitor monitor) {
                try {

                    List<GenericSchema> cubridUsers =
                            wizard.getSettings().getDataSource().getCubridUsers(monitor);
                    UIUtils.syncExec(
                            () -> {
                                tableTables.removeAll();
                                for (GenericSchema user : cubridUsers) {
                                    try {
                                        for (GenericTable table : user.getPhysicalTables(monitor)) {

                                            CubridTable cubridTable = (CubridTable) table;
                                            TableItem item = new TableItem(tableTables, SWT.NONE);
                                            item.setImage(DBeaverIcons.getImage(DBIcon.TREE_TABLE));
                                            item.setText(0, cubridTable.getUniqueName());
                                            item.setData(table);
                                            item.setChecked(checkedTables.contains(cubridTable));
                                        }
                                    } catch (DBException e) {
                                        DBWorkbench.getPlatformUI()
                                                .showError(
                                                        "Table List", "Can't read Table list", e);
                                    }
                                }
                            });

                } catch (DBException e) {
                    DBWorkbench.getPlatformUI().showError("User List", "Can't read User list", e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    @Override
    public void activatePage() {
        updateState();
    }

    @Override
    protected void updateState() {
        updateCheckedTables();
    }
    
    @Override
    protected boolean determinePageCompletion() {
        return true;
    }

    @Override
    public boolean canFlipToNextPage() {
        return true;
    }
}

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
package org.jkiss.dbeaver.ext.cubrid.ui.controls;

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
import org.eclipse.swt.widgets.*;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.cubrid.CubridConstants;
import org.jkiss.dbeaver.ext.cubrid.ui.internal.CubridMessages;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIStyles;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CustomSashForm;
import org.jkiss.dbeaver.ui.controls.resultset.AbstractPresentation;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetController;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetCopySettings;
import org.jkiss.utils.CommonUtils;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class CubridInfoStatistic extends AbstractPresentation
{
    private static final Log log = Log.getLog(CubridInfoStatistic.class);
    DBPPreferenceStore store = DBWorkbench.getPlatform().getPreferenceStore();
    private Table table;
    private DBDAttributeBinding curAttribute;
    private Composite control;
    private SashForm planPanel;
    private Text plainText;
    private Text statisticInfo;

    @Override
    public void createPresentation(@NotNull IResultSetController controller, @NotNull Composite parent) {
        super.createPresentation(controller, parent);

        control = UIUtils.createPlaceholder(parent, 1);
        control.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.planPanel = new CustomSashForm(control, SWT.VERTICAL);
        this.planPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        final GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        this.planPanel.setLayout(gl);
        if (!CommonUtils.isEmpty(store.getString(CubridConstants.STATISTIC))) {
            table = new Table(planPanel, SWT.MULTI | SWT.FULL_SELECTION);
            table.setLinesVisible(!UIStyles.isDarkTheme());
            table.setHeaderVisible(true);
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            table.addSelectionListener(new SelectionAdapter()
            {
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
        } else {
            statisticInfo = new Text(this.planPanel, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
            statisticInfo.setText(String.format(CubridMessages.statistic_instruction_message, CubridMessages.statistic_info + "|" + CubridMessages.statistic_all_info));
        }
        plainText = new Text(this.planPanel, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        plainText.setText(String.format(CubridMessages.statistic_instruction_message, CubridMessages.statistic_trace_info));
        this.readStatistic(controller.getContainer().toString());

    }

    @Override
    public Control getControl() {
        return control;
    }


    private void showStatistic(JDBCResultSet resultSet) throws SQLException {
        table.removeAll();
        while (resultSet.next()) {
            TableItem item = new TableItem(table, SWT.LEFT);
            item.setText(0, resultSet.getString("variable"));
            item.setText(1, resultSet.getString("value"));
        }
        UIUtils.packColumns(table);
    }

    @Override
    public void refreshData(boolean refreshMetadata, boolean append, boolean keepState) {
        this.readStatistic(controller.getContainer().toString());
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
        DBPPreferenceStore store = DBWorkbench.getPlatform().getPreferenceStore();
        new AbstractJob("Read Statistic")
        {
            @Override
            protected IStatus run(DBRProgressMonitor monitor) {

                UIUtils.syncExec(
                        () -> {
                            try (JDBCSession session = DBUtils.openMetaSession(monitor, controller.getDataContainer().getDataSource(), "Read Statistic")) {
                                JDBCStatement stmn = session.createStatement();
                                stmn.execute(query);

                                if (store.getBoolean(CubridConstants.STATISTIC_TRACE)) {
                                    JDBCResultSet resultSet = stmn.executeQuery("show trace;");
                                    if (resultSet.next()) {
                                        String st = resultSet.getString("trace");
                                        plainText.setText(st);
                                    }
                                }
                                if (store.getString(CubridConstants.STATISTIC).equals(CubridConstants.STATISTIC_INFO)) {
                                    JDBCResultSet resultSet = stmn.executeQuery("show exec statistics;");
                                    showStatistic(resultSet);
                                } else if (store.getString(CubridConstants.STATISTIC).equals(CubridConstants.STATISTIC_ALL_INFO)) {
                                    JDBCResultSet resultSet = stmn.executeQuery("show exec statistics all;");
                                    showStatistic(resultSet);
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
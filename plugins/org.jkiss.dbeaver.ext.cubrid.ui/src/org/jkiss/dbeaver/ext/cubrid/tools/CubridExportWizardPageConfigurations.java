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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CustomSashForm;
import org.jkiss.dbeaver.ext.cubrid.task.CubridExportObjectInfo;
import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeToolWizardPage;

class CubridExportWizardPageConfigurations
        extends AbstractNativeToolWizardPage<CubridExportWizard> {
    public int indexCharset;
    public String[] charsets = {
        "UTF-8", "MS949", "ISO-8859-1", "EUC-KR", "EUC-JP", "GB2312", "GBK"
    };
    Path currentRelativePath = Paths.get("");
    String favPath = getPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());

    protected CubridExportWizardPageConfigurations(CubridExportWizard wizard) {
        super(wizard, "Export configuration");
        setTitle("Export configuration");
        setDescription("Set database export settings");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = UIUtils.createPlaceholder(parent, 1);

        Group objectsGroup =
                UIUtils.createControlGroup(
                        composite, "Configuration", 1, GridData.FILL_HORIZONTAL, 0);
        objectsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        SashForm sash = new CustomSashForm(objectsGroup, SWT.VERTICAL);
        sash.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        {
            Group exportPath =
                    UIUtils.createControlGroup(
                            sash, "Objects to Save", 1, GridData.FILL_HORIZONTAL, 0);
            //            objectsGroup.setLayoutData(new GridData(GridData.BEGINNING));

            Composite catPanel = UIUtils.createComposite(exportPath, 8);

            catPanel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

            exportItem(catPanel, "Schema");
            exportItem(catPanel, "Index");
            exportItem(catPanel, "Trigger");
            exportItem(catPanel, "Data");
        }

        {
            Composite catPanel = UIUtils.createComposite(sash, 2);
            final Label textLabel = new Label(catPanel, SWT.NONE);
            textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            textLabel.setText("Font Charset ");

            Combo combo = new Combo(catPanel, SWT.READ_ONLY);
            combo.setItems(charsets);
            combo.setText(charsets[0]);
        }

        {
            Composite catPanel = UIUtils.createComposite(sash, 3);

            final Label textLabel = new Label(catPanel, SWT.NONE);
            textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            textLabel.setText("Location");

            final Text textField = new Text(catPanel, SWT.BORDER);
            textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            textField.setText(favPath);

            Button btn = new Button(catPanel, SWT.NONE);
            btn.setText("Browse");

            btn.addSelectionListener(
                    new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent se) {
                            DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NULL);

                            dialog.setFilterPath(favPath);
                            String path = dialog.open();

                            if (path != null) {

                                favPath = getPath(path);
                                textField.setText(favPath);
                            }
                            wizard.getSettings().setOutputFolderPattern(favPath);
                            updateState();
                        }
                    });
        }
        setControl(composite);
        wizard.getSettings().setOutputFolderPattern(favPath);
    }

    private void exportItem(Composite composite, String label) {
        Button btnCheck = new Button(composite, SWT.CHECK);

        final Label textLabel = new Label(composite, SWT.NONE);
        textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        textLabel.setText(label);

        btnCheck.addListener(
                SWT.Selection,
                new Listener() {
                    public void handleEvent(Event e) {
                       if(btnCheck.getSelection()) {
                           wizard
                         .getSettings()
                         .getExportObjects()
                         .add(new CubridExportObjectInfo(label.toLowerCase(), true));
                       }else {
                           wizard
                           .getSettings()
                           .getExportObjects().removeIf(item->item.getName().equals(label.toLowerCase()));
                       }
                        updateState();
                    }
                });

    }

    @Override
    protected boolean determinePageCompletion() {
        if (!favPath.isEmpty()) {
            for (CubridExportObjectInfo info : this.wizard.getSettings().getExportObjects()) {
                if (info.isExport()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void updateState() {
        saveState();
        updatePageCompletion();
        getContainer().updateButtons();
    }

    private String getPath(String path) {
        return new File(path).getAbsolutePath();
    }
}

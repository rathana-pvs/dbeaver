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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.model.fs.DBFUtils;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.task.DBTTask;
import org.jkiss.dbeaver.registry.task.TaskPreferenceStore;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeExportWizard;
import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeImportExportWizard;
import org.jkiss.dbeaver.tasks.ui.nativetool.internal.TaskNativeUIMessages;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.cubrid.task.CubridExportSettings;
import org.jkiss.dbeaver.ext.cubrid.task.CubridExportObjectInfo;
import org.jkiss.dbeaver.ext.cubrid.task.CubridTasks;

class CubridExportWizard
        extends AbstractNativeImportExportWizard<CubridExportSettings, CubridExportObjectInfo> {

    private CubridExportWizardPageObjects objectsPage;
    private CubridExportWizardPageConfigurations configurationPage;

    CubridExportWizard(DBTTask task) {
        super(task);
    }

    @Override
    protected CubridExportSettings createSettings() {
        return new CubridExportSettings();
    }

    @Override
    public String getTaskTypeId() {
        return CubridTasks.TASK_SCRIPT_EXECUTE;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        super.init(workbench, selection);
        objectsPage = new CubridExportWizardPageObjects(this);
        configurationPage = new CubridExportWizardPageConfigurations(this);
        Object object = selection.getFirstElement();
        if(object instanceof DBNDatabaseItem) {
            object = (CubridUser) ((DBNDatabaseItem) object).getObject();
        }
        
        CubridUser user = (CubridUser) object;
        getSettings().setDataSource((CubridDataSource) user.getDataSource());
    }

    @Override
    public void saveTaskState(
            DBRRunnableContext runnableContext, DBTTask task, Map<String, Object> state) {
        if (objectsPage.getControl() != null) {
            objectsPage.saveState();
        }
        if (configurationPage.getControl() != null) {
            configurationPage.saveState();
        }
        getSettings().saveSettings(runnableContext, new TaskPreferenceStore(state));
    }

    @Override
    public void addPages() {
        addTaskConfigPages();
        addPage(objectsPage);
        addPage(configurationPage);

        super.addPages();
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == configurationPage) {
            return null;
        }
        return super.getNextPage(page);
    }

    @Override
    public IWizardPage getPreviousPage(IWizardPage page) {
        if (page == logPage) {
            return objectsPage;
        }
        return super.getPreviousPage(page);
    }

    
    @Override
    public boolean performFinish() {
//        CubridSettings settings = this.getSettings();
        List<Path> files = new ArrayList<>();
        StringBuilder names = new StringBuilder();
        for (CubridExportObjectInfo info: getSettings().getExportObjects()) {
            try {
                Path dir = DBFUtils.resolvePathFromString(getRunnableContext(), getProject(), getSettings().getOutputFolder(info));
                if (!Files.exists(dir)) {
                    try {
                        Files.createDirectories(dir);
                    } catch (IOException e) {
                        logPage.setMessage("Can't create directory '" + dir.toString() + "': " + e.getMessage(), IMessageProvider.ERROR);
                        getContainer().updateMessage();
                        continue;
                    }
                }
                Path file = DBFUtils.resolvePathFromString(getRunnableContext(), getProject(), getSettings().getOutputFile(info));
                if (!Files.exists(file) || Files.isDirectory(file)) {
                    continue;
                }
                files.add(file);
                if(names.length() == 0) {
                    names.append(info.getName());
                }else {
                    names.append(", ");
                    names.append(info.getName());
                }
                
                
            } catch (DBException e) {
                DBWorkbench.getPlatformUI().showError("Error resolving file", "Error during output file resolution", e);
                return false;
            }
        }
        if(files.size() > 0) {
            boolean deleteFile = UIUtils.confirmAction(
                    "File already exists",
                    String.format("Destination %s already exists. Are you sure you want to override it?", names.toString())
                );
                if (!deleteFile) {
                    return false;
                }
                try {
                    for(Path file : files) {
                        Files.delete(file);
                    }
                    
                } catch (IOException e) {
                    DBWorkbench.getPlatformUI().showError(
                        "Unable to delete file",
                        "Cannot delete file. Try once again",
                        e
                    );
                    return false;
                }
        }
        return super.performFinish();
        
    }
}

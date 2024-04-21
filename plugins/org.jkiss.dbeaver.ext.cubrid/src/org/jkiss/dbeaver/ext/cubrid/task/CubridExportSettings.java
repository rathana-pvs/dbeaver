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
package org.jkiss.dbeaver.ext.cubrid.task;

import java.util.List;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceMap;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.tasks.nativetool.AbstractImportExportSettings;
import org.jkiss.dbeaver.tasks.nativetool.ExportSettingsExtension;

public class CubridExportSettings extends AbstractImportExportSettings<DBSObject>
        implements ExportSettingsExtension<CubridExportObjectInfo> {

    private List<String> tables = new ArrayList<>();
    private List<CubridExportObjectInfo> exportObjects = new ArrayList<>();
    private String taskId;
    private String charset;
    private CubridExportSettings settings;
    private CubridDataSource dataSource;

    @Override
    public List<CubridExportObjectInfo> getExportObjects() {
        return exportObjects;
    }

    @Override
    public String getOutputFile(CubridExportObjectInfo info) {
        return Paths.get(getOutputFolder(info), info.getName()).toString();
    }

    @Override
    public String getOutputFolder(CubridExportObjectInfo info) {
        return getOutputFolderPattern();
    }

    public CubridExportObjectInfo getExportObject(String name) {

        return exportObjects.stream()
                .filter(item -> item.getName().equals(name))
                .findAny()
                .orElse(null);
    }


    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public void saveSettings(DBRRunnableContext runnableContext, DBPPreferenceStore store) {
        super.saveSettings(runnableContext, store);
        if (store instanceof DBPPreferenceMap) {
            ((DBPPreferenceMap) store).getPropertyMap().put("settings", this);
        }
    }

    @Override
    public void loadSettings(DBRRunnableContext runnableContext, DBPPreferenceStore store)
            throws DBException {
        super.loadSettings(runnableContext, store);
        if (store instanceof DBPPreferenceMap) {
            this.setSettings((CubridExportSettings)
                    ((DBPPreferenceMap) store).getPropertyMap().get("settings"));
        }
    }

    public CubridDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(CubridDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public CubridExportSettings getSettings() {
        return settings;
    }

    public void setSettings(CubridExportSettings settings) {
        this.settings = settings;
    }
}

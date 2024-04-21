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

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.ext.cubrid.CubridDataSourceProvider;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.ext.cubrid.task.CubridTasks;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.task.DBTTask;
import org.jkiss.dbeaver.model.task.DBTTaskType;
import org.jkiss.dbeaver.tasks.ui.DBTTaskConfigPanelProvider;
import org.jkiss.dbeaver.tasks.ui.DBTTaskConfigurator;
import org.jkiss.dbeaver.tasks.ui.nativetool.NativeToolConfigPanel;
import org.jkiss.dbeaver.tasks.ui.wizard.TaskConfigurationWizard;

public class CubridTaskConfigurator implements DBTTaskConfigurator, DBTTaskConfigPanelProvider {

    @Override
    public TaskConfigurationWizard<?> createTaskConfigWizard(DBTTask taskConfiguration) {
        switch (taskConfiguration.getType().getId()) {
            case CubridTasks.TASK_SCRIPT_EXECUTE:
                return new CubridExportWizard(taskConfiguration);
        }
        return null;
    }

    @Override
    public ConfigPanel createInputConfigurator(
            DBRRunnableContext runnableContext, @NotNull DBTTaskType taskType) {
        return new ConfigPanel(runnableContext, taskType);
    }

    private static class ConfigPanel extends NativeToolConfigPanel<CubridDataSource> {
        ConfigPanel(DBRRunnableContext runnableContext, DBTTaskType taskType) {
            super(
                    runnableContext,
                    taskType,
                    CubridDataSource.class,
                    CubridDataSourceProvider.class);
        }
    }
}

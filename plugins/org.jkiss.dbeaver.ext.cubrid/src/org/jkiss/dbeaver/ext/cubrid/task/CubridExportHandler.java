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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableContext;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.task.DBTTask;
import org.jkiss.dbeaver.registry.task.TaskPreferenceStore;
import org.jkiss.dbeaver.tasks.nativetool.AbstractNativeToolHandler;
import org.jkiss.utils.CommonUtils;

public class CubridExportHandler
        extends AbstractNativeToolHandler<CubridExportSettings, DBSObject, CubridExportObjectInfo> {
    private String taskErrorMessage;

    @Override
    public Collection<CubridExportObjectInfo> getRunInfo(CubridExportSettings settings) {
        // TODO Auto-generated method stub
        return settings.getExportObjects();
    }

    @Override
    protected CubridExportSettings createTaskSettings(DBRRunnableContext context, DBTTask task)
            throws DBException {
        // TODO Auto-generated method stub
        CubridExportSettings settings = new CubridExportSettings();
        settings.loadSettings(context, new TaskPreferenceStore(task));

        return settings;
    }

    @Override
    protected List<String> getCommandLine(CubridExportSettings settings, CubridExportObjectInfo arg)
            throws IOException {
        // TODO Auto-generated method stub
        List<String> cmd = new ArrayList<>();
        cmd.add("./");
        //		cmd.add(settings.getFavPath());
        return cmd;
    }

    @Override
    public void fillProcessParameters(
            CubridExportSettings settings, CubridExportObjectInfo arg, List<String> cmd)
            throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void startProcessHandler(
            DBRProgressMonitor monitor,
            DBTTask task,
            CubridExportSettings settings,
            final CubridExportObjectInfo arg,
            ProcessBuilder processBuilder,
            Process process,
            Log log)
            throws IOException {

        File dir = new File(settings.getOutputFolderPattern());
        if (dir.exists() || dir.mkdirs()) {
            exportTask(monitor, settings.getSettings(), log);
        }
    }

    void exportTask(DBRProgressMonitor monitor, CubridExportSettings settings, Log log) {
        CubridExportExecution execute =
                new CubridExportExecution(monitor, settings.getDataSource(), settings, log);
        List<CubridExportObjectInfo> items = settings.getExportObjects();
        for (CubridExportObjectInfo co : items) {
            if (co.isExport()) {
                switch (co.getName()) {
                    case "data":
                        execute.exportData(co);
                        break;
                    case "trigger":
                        execute.exportTrigger(co);
                        break;
                    case "index":
                        execute.exportIndex(co);
                        break;
                    case "schema":
                        execute.exportSchema(co);
                        break;
                }
            }
        }
    }

    @Override
    public boolean executeProcess(
            DBRProgressMonitor monitor,
            DBTTask task,
            CubridExportSettings settings,
            CubridExportObjectInfo arg,
            Log log)
            throws IOException, InterruptedException {
        monitor.beginTask(task.getType().getName(), 1);
        try {
            monitor.subTask("Start native tool");
            startProcessHandler(monitor, task, settings, arg, null, null, log);
            monitor.subTask("Executing");
            Thread.sleep(100);

            // process.waitFor();
        } catch (IOException e) {
            log.error("IO error: " + e.getMessage());
            throw e;
        } finally {
            monitor.done();
        }
        return CommonUtils.isEmpty(taskErrorMessage);
    }

    @Override
    protected boolean isNativeClientHomeRequired() {
        return false;
    }

    @Override
    protected boolean doExecute(
            DBRProgressMonitor monitor, DBTTask task, CubridExportSettings settings, Log log)
            throws DBException, InterruptedException {

        long startTime = System.currentTimeMillis();

        boolean isSuccess = true;
        try {

            if (!executeProcess(monitor, task, settings, null, log)) {
                isSuccess = false;
            }

        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw new DBException("Error executing process", e);
        }
        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }

        long workTime = System.currentTimeMillis() - startTime;
        notifyToolFinish(
                task.getType().getName() + " - " + task.getName() + " has finished", workTime);
        return isSuccess;
    }
}

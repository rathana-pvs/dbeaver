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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.ext.cubrid.model.CubridTable;
import org.jkiss.dbeaver.ext.cubrid.model.CubridTrigger;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.ext.generic.model.GenericTableConstraintColumn;
import org.jkiss.dbeaver.ext.generic.model.GenericTableForeignKey;
import org.jkiss.dbeaver.ext.generic.model.GenericTableIndex;
import org.jkiss.dbeaver.ext.generic.model.GenericTableIndexColumn;
import org.jkiss.dbeaver.ext.generic.model.GenericUniqueKey;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.DBStructUtils;
import org.jkiss.dbeaver.runtime.DBWorkbench;

public class CubridExportExecution {

    String numberType = "integer,float,double,short,long".toUpperCase();
    String dateType = "date".toUpperCase();

    private DBRProgressMonitor monitor;
    private CubridDataSource dataSource;
    private CubridExportSettings settings;
    private List<CubridTable> tables = new ArrayList<>();
    private Log log;
    public CubridExportExecution(
            DBRProgressMonitor monitor,
            CubridDataSource dataSource,
            CubridExportSettings settings, Log log) {
        this.monitor = monitor;
        this.dataSource = dataSource;
        this.settings = settings;
        this.log = log;
        this.extractTable();
    }

    public void exportData(CubridExportObjectInfo info) {
        StringBuilder builder = new StringBuilder();
        for(CubridTable table : tables) {
            builderTable(builder, table);
        }
        saveFile(builder, info);
    }

    public void exportSchema(CubridExportObjectInfo info) {
        StringBuilder builder = new StringBuilder();
        for(CubridTable table : tables) {
            buildTableSchema(builder, table);
        }
        
        saveFile(builder, info);
    }

    public void exportIndex(CubridExportObjectInfo info) {
        StringBuilder builder = new StringBuilder();
        for(CubridTable table : tables) {
            buildIndex(builder, table);
        }
        
        saveFile(builder, info);
    }

    public void exportTrigger(CubridExportObjectInfo info) {
        StringBuilder builder = new StringBuilder();
        for(CubridTable table : tables) {
            getTrigger(builder, table);
        }
        saveFile(builder, info);
    }

    private void buildIndex(@NotNull StringBuilder builder, CubridTable table) {

        try {
                List<GenericTableIndex> indexes =
                        (List<GenericTableIndex>) table.getIndexes(monitor);

                if (indexes == null) {
                    return;
                }

                for (GenericTableIndex index : indexes) {
                    log.info(String.format("export index %s", table.getUniqueName()));
                    GenericTableForeignKey fk = table.getAssociation(monitor, index.getName());
                    GenericUniqueKey cons = table.getConstraint(monitor, index.getName());
                    List<GenericTableIndexColumn> cols = index.getAttributeReferences(monitor);
                    GenericUniqueKey pk = null;
                    if (cons != null) {
                        if (cons.getConstraintType() == DBSEntityConstraintType.PRIMARY_KEY) {
                            pk = cons;
                        }
                    }

                    if (pk != null) {
                        StringBuilder colBuilder = new StringBuilder();
                        for (int i = 0; i < cols.size(); i++) {
                            if (i != cols.size() - 1) {
                                colBuilder.append(cols.get(i).getName() + ", ");
                            } else {
                                colBuilder.append(cols.get(i).getName());
                            }
                        }

                        builder.append(
                                String.format(
                                        "ALTER TABLE %s ADD CONSTRAINT %s PRIMARY KEY (%s); \r\n",
                                        table.getUniqueName(), index.getName(), colBuilder));

                    } else if (fk != null) {

                        String deleteRule = fk.getDeleteRule().getName();
                        String updateRule = fk.getUpdateRule().getName();
                        CubridTable refTab = (CubridTable) fk.getReferencedTable();
                        String col = fk.getAttributeReferences(monitor).get(0).getName();

                        StringBuilder buildRefCol = null;
                        for (GenericUniqueKey key : refTab.getConstraints(monitor)) {
                            if (key.getConstraintType() == DBSEntityConstraintType.PRIMARY_KEY) {
                                List<GenericTableConstraintColumn> refCols =
                                        key.getAttributeReferences(monitor);
                                buildRefCol = new StringBuilder();
                                for (int i = 0; i < refCols.size(); i++) {
                                    if (i != refCols.size() - 1) {
                                        buildRefCol.append(refCols.get(i).getName() + ", ");
                                    } else {
                                        buildRefCol.append(refCols.get(i).getName());
                                    }
                                }
                            }
                        }
                        builder.append(
                                String.format(
                                        "ALTER TABLE %s ADD FOREIGN KEY (%s) "
                                                + "REFERENCES %s(%s) ON DELETE %s ON UPDATE %s; \r\n",
                                        table.getUniqueName(),
                                        col,
                                        refTab.getUniqueName(),
                                        buildRefCol,
                                        deleteRule,
                                        updateRule));
                    } else {
                        StringBuilder colBuilder = new StringBuilder();
                        for (int i = 0; i < cols.size(); i++) {
                            String rule = cols.get(i).isAscending() ? "" : " DESC";
                            if (i != cols.size() - 1) {

                                colBuilder.append(cols.get(i).getName() + rule + ", ");

                            } else {
                                colBuilder.append(cols.get(i).getName() + rule);
                            }
                        }
                        builder.append("CREATE ");
                        if (index.isUnique()) {
                            builder.append("UNIQUE ");
                        }
                        //						if(index.isReverse()) {
                        //							builder.append("REVERSE ");
                        //						}
                        builder.append(
                                String.format(
                                        "INDEX [%s] on [%s] (%s) ",
                                        index.getName(), table.getUniqueName(), colBuilder));

                        if (index.getDescription() != null) {
                            builder.append("COMMENT = " + index.getDescription());
                        }
                        builder.append("\r\n");
                    }
                }

        } catch (DBException e) {
            // TODO Auto-generated catch block
            DBWorkbench.getPlatformUI().showError("Table List", "Can't read Table list", e);
        }
    }

    /**
     * get Trigger DDL
     *
     * @return DDL String
     */
    private void getTrigger(@NotNull StringBuilder buf, CubridTable table) {
        String newLine = "\r\n";
        String endLineChar = ";";

        List<CubridTrigger> triggers;
        try {
           
                triggers = (List<CubridTrigger>) table.getOldSchema().getTriggers(monitor);
                if (triggers == null) {
                    return;
                }

                for (CubridTrigger trigger : triggers) {
                    log.info(String.format("export trigger  %s", trigger.getName()));
                    // CREATE TRIGGER trigger_name
                    buf.append("CREATE TRIGGER ");
                    String triggerName = trigger.getName();

                    buf.append('"').append(triggerName).append('"');

                    buf.append(newLine);

                    // [ STATUS { ACTIVE | INACTIVE } ]
                    Integer status = trigger.getStatus();

                    if (status == 1) {
                        buf.append("STATUS INACTIVE");
                        buf.append(newLine);
                    }

                    // event_time event_type [ event_target ]
                    StringBuffer ifBF = new StringBuffer();
                    StringBuffer execBF = new StringBuffer();

                    String conditionTime = trigger.getConditionTime();
                    String eventType = trigger.getEvent();
                    String targetTable = trigger.getTable().getUniqueName();
                    String targetColumn = trigger.getTargetAttribute();

                    String condition = trigger.getCondition();
                    String actionTime = trigger.getActionTime();

                    // EXECUTE [ AFTER | DEFERRED ] action [ ; ]
                    execBF.append("EXECUTE ");

                    if (isEmpty(condition)) {
                        if ("AFTER".equals(actionTime) || "DEFERRED".equals(actionTime)) {
                            buf.append(actionTime);
                        } else {
                            buf.append("BEFORE");
                        }
                    } else {
                        buf.append(conditionTime);

                        // [ IF condition ]
                        ifBF.append("IF ").append(condition);
                        ifBF.append(newLine);
                        execBF.append(actionTime);
                    }

                    buf.append(' ').append(eventType).append(' ');

                    if (isEmpty(targetTable)) {
                        if (!("COMMIT".equals(eventType) || "ROLLBACK".equals(eventType))) {
                            buf.append("<event_target>");
                        }
                    } else {
                        buf.append("ON \"").append(targetTable).append('"');

                        if (!isEmpty(targetColumn)) {
                            buf.append("(\"").append(targetColumn).append("\")");
                        }
                    }

                    buf.append(newLine);
                    buf.append(ifBF.toString());
                    buf.append(execBF.toString());
                    buf.append(' ');
                    String actionType = trigger.getActionType();

                    if ("REJECT".equals(actionType)
                            || "INVALIDATE TRANSACTION".equals(actionType)) {
                        buf.append(actionType);
                    } else if ("PRINT".equals(actionType)) {
                        buf.append(actionType);
                        buf.append(newLine);

                        if (trigger.getActionDefinition() != null) {
                            buf.append('\'')
                                    .append(trigger.getActionDefinition().replace("'", "''"))
                                    .append('\'');
                        }
                    } else {
                        buf.append(newLine);

                        if (trigger.getActionDefinition() != null) {
                            buf.append(trigger.getActionDefinition());
                        }
                    }

                    buf.append(endLineChar).append(newLine).append(newLine);
                }

        } catch (DBException e) {
            DBWorkbench.getPlatformUI().showError("Table List", "Can't read Table list", e);
        }
    }

    private void buildTableSchema(@NotNull StringBuilder builder, CubridTable table) {
        try {
            
            log.info(String.format("export schema for table %s", table.getUniqueName()));
            String d = table.getDDL();
            String[] rawDDL =
                    DBStructUtils.generateTableDDL(monitor, table, null, false).split("\n");
            String ddl = String.join("\n", Arrays.copyOfRange(rawDDL, 4, rawDDL.length));
            builder.append(ddl + "\n\n");
           
        } catch (DBException e) {
            DBWorkbench.getPlatformUI().showError("Table List", "Can't read Table list", e);
        }
    }

    private void buildColumnName(
            @NotNull ResultSetMetaData metaData,
            @NotNull StringBuilder builder,
            @NotNull String name)
            throws SQLException {
        builder.append("%class " + '\"' + name + "\" (");
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            builder.append('\"' + metaData.getColumnName(i) + '\"');
            if (i != metaData.getColumnCount()) {
                builder.append(" ");
            }
        }
        builder.append(")\n");
    }

    private void buildColumnData(
            @NotNull JDBCResultSet dbResult,
            @NotNull StringBuilder builder,
            @NotNull int colIndex) {

        try {
            ResultSetMetaData metaData = dbResult.getMetaData();
            String type;
            type = metaData.getColumnTypeName(colIndex);
            if (numberType.contains(type)) {
                builder.append(JDBCUtils.safeGetString(dbResult, colIndex));
            } else if (dateType.contains(type)) {
                builder.append("data\' " + JDBCUtils.safeGetString(dbResult, colIndex) + "\'");
            } else {
                builder.append("\'" + JDBCUtils.safeGetString(dbResult, colIndex) + "\'");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            DBWorkbench.getPlatformUI()
                    .showError("Column metadata", "Can't read Column metadata", e);
        }
    }

    private void builderTable(@NotNull StringBuilder builder, CubridTable table) {
        try {
                log.info(String.format("export data for table %s", table.getUniqueName()));
                try (JDBCSession session =
                        DBUtils.openMetaSession(
                                monitor, dataSource.getParentObject(), "Load table")) {
                    JDBCStatement dbState =
                            session.prepareStatement("select * from " + table.getUniqueName());
                    dbState.executeStatement();
                    JDBCResultSet dbResult = dbState.getResultSet();

                    buildColumnName(dbResult.getMetaData(), builder, table.getUniqueName());
                    int columnCount = dbResult.getMetaData().getColumnCount();
                    while (dbResult.next()) {

                        if (monitor.isCanceled()) {
                            break;
                        }

                        for (int i = 1; i <= columnCount; i++) {
                            buildColumnData(dbResult, builder, i);
                            if (i != columnCount) {
                                builder.append(" ");
                            }
                        }

                        builder.append("\n");
                    }

                } catch (SQLException e) {
                    throw new DBException(e, dataSource);
                }
            

        } catch (DBException e) {
            DBWorkbench.getPlatformUI().showError("Table List", "Can't read Table list", e);
        }
    }

    private void saveFile(@NotNull StringBuilder builder, @NotNull CubridExportObjectInfo info) {

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(settings.getOutputFile(info)))) {
            writer.append(builder);
            builder.delete(0, builder.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractTable() {
        try {
            for (String name : settings.getTables()) {
                String[] values = name.split("\\.");
                if (values.length == 2) {
                    CubridUser user = (CubridUser) this.dataSource.getSchema(values[0]);
                    CubridTable table = (CubridTable) user.getTable(monitor, values[1]);
                    tables.add(table);
                } else {
                    for (GenericSchema user : this.dataSource.getCubridUsers(monitor)) {
                        CubridTable table = (CubridTable) user.getTable(monitor, name);
                        if (table != null) {
                            tables.add(table);
                            break;
                        }
                    }
                }
            }

        } catch (DBException e) {
            DBWorkbench.getPlatformUI().showError("Table List", "Can't read Table list", e);
        }
    }

    @NotNull
    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}

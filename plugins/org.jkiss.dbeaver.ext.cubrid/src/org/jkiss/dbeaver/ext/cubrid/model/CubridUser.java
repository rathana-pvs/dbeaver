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
package org.jkiss.dbeaver.ext.cubrid.model;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.cubrid.CubridConstants;
import org.jkiss.dbeaver.ext.generic.model.GenericDataSource;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.ext.generic.model.GenericStructContainer;
import org.jkiss.dbeaver.ext.generic.model.GenericTable;
import org.jkiss.dbeaver.ext.generic.model.GenericTableBase;
import org.jkiss.dbeaver.ext.generic.model.GenericTableColumn;
import org.jkiss.dbeaver.ext.generic.model.GenericTableIndex;
import org.jkiss.dbeaver.ext.generic.model.GenericTableIndexColumn;
import org.jkiss.dbeaver.ext.generic.model.GenericView;
import org.jkiss.dbeaver.ext.generic.model.TableCache;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCCompositeCache;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.utils.CommonUtils;

public class CubridUser extends GenericSchema implements DBPNamedObject2, DBPSaveableObject
{
    private String name;
    private boolean persisted;
    private final CubridIndexCache cubridIndexCache;
    public CubridUser(
            @NotNull GenericDataSource dataSource,
            @NotNull String schemaName,
            @Nullable String description,
            @Nullable JDBCResultSet dbResult,
            DBRProgressMonitor monitor) {
        super(dataSource, null, schemaName);
        this.name = schemaName;
        this.persisted = dbResult != null;
        this.cubridIndexCache = new CubridIndexCache(this.getTableCache());

        
    }



    @NotNull
    @Property(viewable = true, order = 1)
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }
    @NotNull
    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public void setPersisted(@NotNull boolean persisted) {
        this.persisted = persisted;
    }
   

    @NotNull
    public boolean supportsSystemTable() {
        return name.equals("DBA");
    }

    @NotNull
    public boolean supportsSystemView() {
        return name.equals("DBA");
    }

    @NotNull
    public boolean showSystemTableFolder() {
        return this.getDataSource().getContainer().getNavigatorSettings().isShowSystemObjects();
    }

    @NotNull
    public boolean supportsSynonym() {
        return ((CubridDataSource) this.getDataSource()).getSupportMultiSchema();
    }

    @NotNull
    public boolean supportsTrigger() {
        return CubridConstants.DBA.equals(getDataSource().getContainer().getConnectionConfiguration().getUserName());
    }

    @NotNull
    @Override
    public TableCache createTableCache(@NotNull GenericDataSource datasource) {
        return new CubridTableCache(datasource);
    }

    @NotNull
    public CubridIndexCache getCubridIndexCache() {
        return cubridIndexCache;
    }

    @Nullable
    @Override
    public List<CubridTable> getPhysicalTables(@NotNull DBRProgressMonitor monitor) throws DBException {
        List<CubridTable> tables = new ArrayList<>();
        for (GenericTable table : super.getPhysicalTables(monitor)) {
            if (!table.isSystem()) {
                tables.add((CubridTable) table);
            }
        }
        return tables;
    }

    @Nullable
    public List<? extends CubridTable> getPhysicalSystemTables(@NotNull DBRProgressMonitor monitor)
            throws DBException {
        List<CubridTable> tables = new ArrayList<>();
        for (GenericTable table : super.getPhysicalTables(monitor)) {
            if (table.isSystem()) {
                tables.add((CubridTable) table);
            }
        }
        return tables;
    }

    @Nullable
    @Override
    public List<CubridView> getViews(@NotNull DBRProgressMonitor monitor) throws DBException {
        List<CubridView> views = new ArrayList<>();
        for (GenericView view : super.getViews(monitor)) {
            if (!view.isSystem()) {
                views.add((CubridView) view);
            }
        }
        return views;
    }

    @Nullable
    public List<CubridView> getSystemViews(@NotNull DBRProgressMonitor monitor) throws DBException {
        List<CubridView> views = new ArrayList<>();
        for (GenericView view : super.getViews(monitor)) {
            if (view.isSystem()) {
                views.add((CubridView) view);
            }
        }
        return views;
    }

    @Nullable
    @Override
    public List<GenericTableIndex> getIndexes(@NotNull DBRProgressMonitor monitor) throws DBException {
        List<GenericTableIndex> indexes = new ArrayList<>();
        for (CubridTable table : getPhysicalTables(monitor)) {
            indexes.addAll(table.getIndexes(monitor));
        }
        return indexes;
    }

    public class CubridTableCache extends TableCache
    {
        protected CubridTableCache(@NotNull GenericDataSource dataSource) {
            super(dataSource);
        }

        @NotNull
        @Override
        protected GenericTableColumn fetchChild(
                @NotNull JDBCSession session,
                @NotNull GenericStructContainer owner,
                @NotNull GenericTableBase table,
                @NotNull JDBCResultSet dbResult)
                throws SQLException, DBException {
            String columnName = JDBCUtils.safeGetString(dbResult, "attr_name");
            String dataType = JDBCUtils.safeGetString(dbResult, "data_type");
            boolean autoIncrement = false;
            String tableName = table.isSystem() ? table.getName() : ((CubridDataSource) getDataSource()).getMetaModel().getTableOrViewName(table);
            String sql = "show columns from " + DBUtils.getQuotedIdentifier(getDataSource(), tableName) + " where Field = ?";
            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                dbStat.setString(1, columnName);
                try (JDBCResultSet result = dbStat.executeQuery()) {
                    if (result.next()) {
                        dataType = JDBCUtils.safeGetString(result, "Type");
                        autoIncrement = JDBCUtils.safeGetString(result, "Extra").equals("auto_increment");
                    }
                }
            }
            return new CubridTableColumn(table, columnName, dataType, autoIncrement, dbResult);
        }
    }

    public class CubridIndexCache extends JDBCCompositeCache<GenericStructContainer, CubridTable, GenericTableIndex, GenericTableIndexColumn>
    {
        CubridIndexCache(@NotNull TableCache tableCache) {
            super(tableCache, CubridTable.class, JDBCConstants.TABLE_NAME, JDBCConstants.INDEX_NAME);
        }

        @NotNull
        @Override
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GenericStructContainer owner, @Nullable CubridTable forParent)
                throws SQLException {
            return session.getMetaData().getIndexInfo(null, null, forParent.getUniqueName(), false, true).getSourceStatement();
        }

        @Nullable
        @Override
        protected GenericTableIndex fetchObject(
                @NotNull JDBCSession session,
                @NotNull GenericStructContainer owner,
                @Nullable CubridTable parent,
                @Nullable String indexName,
                @NotNull JDBCResultSet dbResult)
                throws SQLException, DBException {
            boolean isNonUnique = JDBCUtils.safeGetBoolean(dbResult, JDBCConstants.NON_UNIQUE);
            String indexQualifier = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.INDEX_QUALIFIER);
            long cardinality = JDBCUtils.safeGetLong(dbResult, JDBCConstants.INDEX_CARDINALITY);
            int indexTypeNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.TYPE);
            String name = indexName;

            DBSIndexType indexType;
            switch (indexTypeNum) {
                case DatabaseMetaData.tableIndexStatistic:
                    return null;
                case DatabaseMetaData.tableIndexClustered:
                    indexType = DBSIndexType.CLUSTERED;
                    break;
                case DatabaseMetaData.tableIndexHashed:
                    indexType = DBSIndexType.HASHED;
                    break;
                case DatabaseMetaData.tableIndexOther:
                    indexType = DBSIndexType.OTHER;
                    break;
                default:
                    indexType = DBSIndexType.UNKNOWN;
                    break;
            }
            if (CommonUtils.isEmpty(name)) {
                name = parent.getName().toUpperCase(Locale.ENGLISH) + "_INDEX";
            }
            return new GenericTableIndex(parent, isNonUnique, indexQualifier,
                    cardinality, name, indexType, true);
        }

        @Nullable
        @Override
        protected GenericTableIndexColumn[] fetchObjectRow(
                @NotNull JDBCSession session,
                @NotNull CubridTable parent,
                @NotNull GenericTableIndex object,
                @NotNull JDBCResultSet dbResult)
                throws SQLException, DBException {
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, JDBCConstants.ORDINAL_POSITION);
            String columnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.COLUMN_NAME);
            String ascOrDesc = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.ASC_OR_DESC);

            if (CommonUtils.isEmpty(columnName)) {
                return null;
            }
            GenericTableColumn tableColumn = parent.getAttribute(session.getProgressMonitor(), columnName);
            if (tableColumn == null) {
                return null;
            }
            return new GenericTableIndexColumn[]{new GenericTableIndexColumn(
                    object, tableColumn, ordinalPosition, !"D".equalsIgnoreCase(ascOrDesc))
            };
        }

        @Override
        protected void cacheChildren(
                @NotNull DBRProgressMonitor monitor,
                @Nullable GenericTableIndex object,
                @Nullable List<GenericTableIndexColumn> children) {
            object.setColumns(children);
        }
    }

}

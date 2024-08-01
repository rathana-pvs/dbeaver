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
package org.jkiss.dbeaver.ext.cubrid.edit;

import java.util.List;
import java.util.Map;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.cubrid.model.CubridDataSource;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.ext.generic.model.GenericObjectContainer;
import org.jkiss.dbeaver.ext.generic.model.GenericSchema;
import org.jkiss.dbeaver.ext.generic.model.GenericStructContainer;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.utils.CommonUtils;

public class CubridUserManager extends SQLObjectEditor<GenericSchema, GenericStructContainer> /*implements DBEObjectRenamer<OracleSchema>*/ {

    private static final int MAX_NAME_GEN_ATTEMPTS = 100;
    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return FEATURE_EDITOR_ON_CREATE;
    }

    @Nullable
    @Override
    public DBSObjectCache<? extends DBSObject, GenericSchema> getObjectsCache(GenericSchema object) {
        DBSObject parentObject = object.getParentObject();
        if (parentObject instanceof GenericObjectContainer container) {
            return container.getDataSource().getSchemaCache();
        }
        return null;
    }

    @Override
    protected CubridUser createDatabaseObject(
            @NotNull DBRProgressMonitor monitor,
            @NotNull DBECommandContext context,
            @NotNull final Object container,
            @Nullable Object copyFrom,
            @NotNull Map<String, Object> options) {
        
        CubridUser user = new CubridUser((CubridDataSource) container, getNewName((CubridDataSource) container, "NEW_USER"), null, null, monitor);
        if (user.getDataSource().getContainer().getConnectionConfiguration().getUserName().toUpperCase().equals("DBA")) {
            return user;
        } else {
            throw new IllegalArgumentException("Operation add user can only be performed by DBA or a DBA group member.");
        }
    }
    
    private String getNewName(CubridDataSource container, String baseName) {
        
        
        for (int i = 0; i < MAX_NAME_GEN_ATTEMPTS; i++) {
            String newName = i == 0?baseName: baseName + "_" + i;
            if(container.getSchema(newName) == null)
                return newName;
        }
        return baseName;
    }

    @Override
    protected void addObjectCreateActions(
            @NotNull DBRProgressMonitor monitor,
            @NotNull DBCExecutionContext executionContext,
            @NotNull List<DBEPersistAction> actions,
            @NotNull ObjectCreateCommand command,
            @NotNull Map<String, Object> options) {
        CubridUser user = (CubridUser) command.getObject();
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE USER " + user.getName().toUpperCase());
        buildBody(user, builder, command.getProperties());
        actions.add(new SQLDatabasePersistAction("Create schema", builder.toString()));
    }
    
    private void buildBody(CubridUser user, StringBuilder builder, Map<Object, Object> properties) {
        for (Object key : properties.keySet()) {
            switch(key.toString()){
                case "PASSWORD":
                    builder.append(" PASSWORD " + SQLUtils.quoteString(user, properties.get(key).toString()));
                    break;
                case "GROUPS": 
                    List<String> groups = (List<String>) properties.get(key);
                    builder.append(" GROUPS " + String.join(", ", groups));
                    break;
                case "DESCRIPTION": 
                    builder.append(" COMMENT " + SQLUtils.quoteString(user, properties.get(key).toString()));
                default:
                    break;
                
            }
        }
    }

    protected void addObjectModifyActions(
            @NotNull DBRProgressMonitor monitor,
            @NotNull DBCExecutionContext executionContext,
            @NotNull List<DBEPersistAction> actionList,
            @NotNull ObjectChangeCommand command,
            @NotNull Map<String, Object> options) {
        CubridUser user = (CubridUser) command.getObject();
        String sql = "ALTER USER " + user.getName();
        if (!CommonUtils.isEmpty(user.getPassword())) {
            sql += " PASSWORD " + SQLUtils.quoteString(user, user.getPassword());
        }
        if (command.getProperty("description") != null) {
            sql += " COMMENT " + SQLUtils.quoteString(user, user.getDescription());
        }
        actionList.add(new SQLDatabasePersistAction("Alter schema", sql));
    }

    @Override
    protected void addObjectDeleteActions(
            @NotNull DBRProgressMonitor monitor,
            @NotNull DBCExecutionContext executionContext,
            @NotNull List<DBEPersistAction> actions,
            @NotNull ObjectDeleteCommand command,
            @NotNull Map<String, Object> options) {
        actions.add(
            new SQLDatabasePersistAction("Drop schema",
                "DROP USER " + DBUtils.getQuotedIdentifier(command.getObject())));
    }

}

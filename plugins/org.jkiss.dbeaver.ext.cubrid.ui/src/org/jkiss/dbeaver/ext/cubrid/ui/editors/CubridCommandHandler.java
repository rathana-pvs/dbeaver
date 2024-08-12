package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.ext.cubrid.model.CubridPrivilage;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;

public class CubridCommandHandler extends DBECommandComposite <CubridPrivilage, CubridUserHandler>{

    protected CubridCommandHandler(CubridPrivilage object) {
        super(object,"Update User");
    }
    
    @NotNull
    @Override
    public DBEPersistAction[] getPersistActions(@NotNull DBRProgressMonitor monitor, @NotNull DBCExecutionContext executionContext, @NotNull Map<String, Object> options)
    {
        List<DBEPersistAction> actions = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        if(getObject().isPersisted()) {
            builder.append("ALTER USER " + this.getObject().getName());
        }
        buildBody(builder);
        actions.add(new SQLDatabasePersistAction("Update User", builder.toString()));
        return actions.toArray(new DBEPersistAction[0]);
        
    }
    
    
    
    private void buildBody(StringBuilder builder) {
        for (Object key : getProperties().keySet()) {
            switch(key.toString()){
                case "PASSWORD":
                    builder.append(" PASSWORD " + SQLUtils.quoteString(getObject(), this.getProperty(key).toString()));
                    break;
                case "DESCRIPTION": 
                    builder.append(" COMMENT " + SQLUtils.quoteString(getObject(), this.getProperty(key).toString()));
                default:
                    break;
                
            }
        }
    }
    



    
    
}

package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;

public class CubridCommandHandler extends DBECommandComposite <CubridUser, CubridUserHandler>{

    protected CubridCommandHandler(CubridUser object) {
        super(object,"Update User");
        // TODO Auto-generated constructor stub
    }
    
    @NotNull
    @Override
    public DBEPersistAction[] getPersistActions(@NotNull DBRProgressMonitor monitor, @NotNull DBCExecutionContext executionContext, @NotNull Map<String, Object> options)
    {
        List<DBEPersistAction> actions = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        if(getObject().isPersisted()) {
            builder.append("ALTER USER " + getUserName());
        }else {
            builder.append("CREATE USER " +  getUserName());
        }
        buildBody(builder);
        actions.add(new SQLDatabasePersistAction("Update User", builder.toString()));
        return actions.toArray(new DBEPersistAction[0]);
        
    }
    
    private void createUserScript(StringBuilder builder) {
        
        builder.append("CREATE USER " +  getUserName());
        buildBody(builder);
        
    }
    
    public String getUserName() {
        Object name = this.getProperty(CubridUserHandler.NAME.getId());
        if(name != null) {
            return (String) name;
        }
        return this.getObject().getName();
    }
    
    private void buildBody(StringBuilder builder) {
        for (Object key : getProperties().keySet()) {
            switch(key.toString()){
                case "PASSWORD":
                    builder.append(" PASSWORD " + SQLUtils.quoteString(getObject(), this.getProperty(key).toString()));
                    break;
                case "GROUPS": 
                    List<String> groups = (List<String>) this.getProperty(key);
                    builder.append(" GROUPS " + String.join(", ", groups));
                    break;
                case "DESCRIPTION": 
                    builder.append(" COMMENT " + SQLUtils.quoteString(getObject(), this.getProperty(key).toString()));
                default:
                    break;
                
            }
        }
    }
    
    private void updateUserScript(StringBuilder builder) {
        
        builder.append("ALTER USER " + getUserName());
        buildBody(builder);
    }


    
    
}

package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import org.jkiss.dbeaver.ext.cubrid.model.CubridPrivilage;
import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.edit.prop.DBEPropertyHandler;
import org.jkiss.dbeaver.model.edit.prop.DBEPropertyReflector;

public enum CubridUserHandler implements DBEPropertyHandler<CubridPrivilage>, DBEPropertyReflector<CubridPrivilage>{
    NAME,
    PASSWORD,
    GROUPS,
    DESCRIPTION;
    @Override
    public void reflectValueChange(CubridPrivilage object, Object oldValue, Object newValue) {
        // TODO Auto-generated method stub
        if(this == NAME) {
            object.setName(newValue.toString());
        }
        
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return name();
    }

    @Override
    public DBECommandComposite createCompositeCommand(CubridPrivilage object) {
        // TODO Auto-generated method stub
        return new CubridCommandHandler(object);
    }
    
    
    
}

package org.jkiss.dbeaver.ext.cubrid.ui.editors;

import org.jkiss.dbeaver.ext.cubrid.model.CubridUser;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.edit.prop.DBEPropertyHandler;
import org.jkiss.dbeaver.model.edit.prop.DBEPropertyReflector;

public enum CubridUserHandler implements DBEPropertyHandler<CubridUser>, DBEPropertyReflector<CubridUser>{
    NAME,
    PASSWORD,
    GROUPS,
    DESCRIPTION;
    @Override
    public void reflectValueChange(CubridUser object, Object oldValue, Object newValue) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return name();
    }

    @Override
    public DBECommandComposite createCompositeCommand(CubridUser object) {
        // TODO Auto-generated method stub
        return new CubridCommandHandler(object);
    }
    
    
    
}

package org.jkiss.dbeaver.ext.cubrid.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;

public class CubridPrivilage implements DBSObject{
    private String name;
    private String description;
    private boolean persisted = false;
    private CubridDataSource container;
    private List<String> groups = new ArrayList<>();
    
    public CubridPrivilage(@NotNull CubridDataSource container, @NotNull String name, @Nullable JDBCResultSet dbResult){
        this.container = container;
        this.name = name;
        if(dbResult != null) {
            persisted = true;
            description = JDBCUtils.safeGetString(dbResult, "comment");
            
            if(dbResult != null) {
                String sql = "select t.groups.name from db_user join table(groups) as t(groups) where name = ?";
                try (JDBCPreparedStatement dbStat = dbResult.getSession().prepareStatement(sql)) {
                    dbStat.setString(1, name);
                    try (JDBCResultSet result = dbStat.executeQuery()) {
                        while (result.next()) {
                            groups.add(JDBCUtils.safeGetString(result, "groups.name"));
                        }
                        
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
       this.name = name;
    }
    

    @Override
    public boolean isPersisted() {
        // TODO Auto-generated method stub
        return persisted;
    }
    
    @Nullable
    public List<String> getRoles(){
        return groups;
    }
    
    @Nullable
    @Property(viewable = true, order = 2)
    public String getGroups() {
        return String.join(", ", groups);
    }
    
    
    @Nullable
    @Override
    @Property(viewable = true, order = 3)
    public String getDescription() {
        // TODO Auto-generated method stub
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public CubridDataSource getParentObject() {
        // TODO Auto-generated method stub
        return this.getDataSource();
    }

    @Override
    public CubridDataSource getDataSource() {
        // TODO Auto-generated method stub
        return this.container;
    }
    
}

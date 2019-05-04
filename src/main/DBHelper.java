
package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DBHelper {
    
    private String queryBuilder = "";
    private String tableName = "";
    private String pkName = "";
    private HashMap<String, Object> tableMapper =  new LinkedHashMap();
    private HashMap<String, Object> whereClauseMapper = new LinkedHashMap();
    private boolean isUpdate = false;
    private boolean hasWhere = false;
    private int updateId = 0;
    private PreparedStatement ps = null;
    private Connection con = null;
    
    public DBHelper(String tableName) {
        this.tableName = tableName;
    }
    
    public DBHelper(String tableName, int id) {
        this.tableName = tableName;
        this.isUpdate = true;
        this.updateId = id;
    }
    
    public DBHelper(String tableName, int id, String pkName) {
        this.tableName = tableName;
        this.isUpdate = true;
        this.updateId = id;
        this.pkName = pkName;
    }
    
    public void connect() throws ClassNotFoundException, SQLException {
        String dbServer = "jdbc:mysql://" + Config.server + ":" +  Config.db_port + "/" + Config.database;
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(dbServer, Config.username, Config.password);
    }
    
    public void set(String columnName, Object value) throws SQLException {
        tableMapper.put(columnName, value);
        queryBuilder = "";
        hasWhere = false;
        
        if(!isUpdate) {
            
            queryBuilder = "insert into " + tableName + "(";
            
            int i = 0;
            for(String column : tableMapper.keySet()) {
                queryBuilder += i < tableMapper.keySet().size() - 1 ? column + "," : column;
                i++;
            }
            
            i = 0;
            queryBuilder += ") VALUES (";
            
            for(Object mapValue : tableMapper.entrySet()) {
                queryBuilder += i < tableMapper.entrySet().size() - 1 ? "?," : "?)";
                i++;
            }
            
        } else {
            queryBuilder = "update " + tableName + " set ";
            
            int i = 0;
            for(String column : tableMapper.keySet()) {
                queryBuilder += i < tableMapper.keySet().size() - 1 ? column + " = ?, " : column + " = ?";
                i++;
            }
            
            where(pkName.isEmpty() ? "id" : pkName, updateId);
            
        }
    }
    
    public void save() throws SQLException {
        ps = con.prepareStatement(queryBuilder);
            
        int i = 1;
        for(Map.Entry m : tableMapper.entrySet()) {
            ps.setObject(i, m.getValue());
            i++;
        }
        
        for(Map.Entry m : whereClauseMapper.entrySet()) {
            ps.setObject(i, m.getValue());
            i++;
        }

        // show query and data
        System.out.println(queryBuilder);
        showData();
        
        // execute query
        ps.executeUpdate();
        
        // clean up
        hasWhere = true;
        tableMapper.clear();
        whereClauseMapper.clear();
        con.close();
    }
    
    public DBHelper where(String columnName, Object value) {
        whereClauseMapper.put(columnName, value);
        
        if(!hasWhere) {
            queryBuilder += " where " + columnName + " = ? ";
            hasWhere = true;
        } else {
            queryBuilder += " and " + columnName + " = ? ";
        }
        
        return this;
    }
    
    public DBHelper whereNotEqual(String columnName, Object value) {
        whereClauseMapper.put(columnName, value);
        
        if(!hasWhere) {
            queryBuilder += " where " + columnName + " != ? ";
            hasWhere = true;
        } else {
            queryBuilder += " and " + columnName + " != ? ";
        }
        
        return this;
    }
    
    public DBHelper whereIsNotNull(String columnName) {
        whereClauseMapper.put(columnName, null);
        
        if(!hasWhere) {
            queryBuilder += " where " + columnName + " is not ? ";
            hasWhere = true;
        } else {
            queryBuilder += " and " + columnName + " is not ? ";
        }
        
        return this;
    }
    
    public DBHelper orWhere(String columnName, Object value) {
        whereClauseMapper.put(columnName, value);
        
        if(!hasWhere) {
        } else {
            queryBuilder += " or " + columnName + " = ? ";
        }
        
        return this;
    }
    
    public void delete() throws SQLException {
        queryBuilder += "delete from " + tableName;
        where("id", updateId);
        save();
    }
    
    public void showData() {
        for ( Map.Entry<String, Object> entry : tableMapper.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            System.out.println("key: " + key + "\t" + "value: " + value);
        }
        for ( Map.Entry<String, Object> entry : whereClauseMapper.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            System.out.println("key: " + key + "\t" + "value: " + value);
        }
    }
    
    
}

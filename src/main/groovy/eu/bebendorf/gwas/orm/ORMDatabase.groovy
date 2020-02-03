package eu.bebendorf.gwas.orm

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement
import java.sql.Time
import java.sql.Timestamp

class ORMDatabase {

    private Connection connection = null
    private String connectionString
    private Map<ResultSet, PreparedStatement> statementMap = [:]

    ORMDatabase(Map<String, Object> settings){
        if(!settings.containsKey('host')){
            settings['host'] = 'localhost'
        }
        if(!settings.containsKey('user')){
            if(settings.containsKey('username')){
                settings['user'] = settings['username']
                settings['username'] = ''
            }
        }
        if(!settings.containsKey('user')){
            settings['user'] = settings['database']
        }
        if(!settings.containsKey('autoReconnect')){
            settings['autoReconnect'] = true
        }
        if(!settings.containsKey('failOverReadOnly')){
            settings['failOverReadOnly'] = false
        }
        if(!settings.containsKey('characterEncoding')){
            settings['characterEncoding'] = 'UTF-8'
        }
        if(!settings.containsKey('useUnicode')){
            settings['useUnicode'] = 'yes'
        }
        if(!settings.containsKey('maxReconnects')){
            settings['maxReconnects'] = 5
        }
        List<String> properties = []
        for(key in settings.keySet()){
            if(key == "database" || key == "host")
                continue
            properties.add(key+"="+settings[key])
        }
        connectionString = "jdbc:mysql://" + settings.host + "/" + settings.database + "?" + String.join('&', properties)
    }

    private Connection connect(){
        try {
            if(connection==null||connection.isClosed()){
                try {
                    Class.forName("com.mysql.jdbc.Driver")
                    connection = DriverManager.getConnection(connectionString)
                } catch (SQLException e) {
                    throw new RuntimeException("Error while connecting to the database: " + e.getMessage())
                } catch (ClassNotFoundException ignored) {
                    throw new RuntimeException("Error while connecting to the database (driver not found)")
                }
            }
        } catch (SQLException e) {e.printStackTrace();}
        try {
            if(connection!=null&&connection.isClosed()){
                connection = null
            }
        } catch (SQLException ignored) {
            connection = null
        }
        return connection
    }

    int insert(String table, Map<String, Object> data){
        List<String> columns = []
        Object[] valuesArray = new Object[data.size()]
        StringBuilder sb = new StringBuilder()
        int ci = 0
        for(key in data.keySet()){
            columns.add('`'+sqlifyName(key)+'`')
            valuesArray[ci] = data[key]
            sb.append(',?')
            ci++
        }
        try {
            PreparedStatement ps = setParams(connect().prepareStatement("INSERT INTO `"+table+"` ("+String.join(',',columns)+") VALUES ("+sb.toString().substring(1)+");", Statement.RETURN_GENERATED_KEYS), valuesArray)
            ps.executeUpdate()
            ResultSet rs = ps.getGeneratedKeys()
            if(rs.next()){
                int id = rs.getInt(1)
                ps.close()
                return id
            }
            ps.close()
        } catch (SQLException e) {e.printStackTrace()}
        0
    }

    void delete(String table, String where, Object... values){
        try {
            PreparedStatement ps = setParams(connect().prepareStatement("DELETE FROM `"+table+"`"+(where!=null?" WHERE "+sqlifyNames(where)+";":";")), values)
            ps.executeUpdate()
            ps.close()
        } catch (SQLException e) {e.printStackTrace()}
    }

    void update(String table, Map<String, Object> values){
        Map<String, Object> valuesClone = values.clone()
        int id = values['id']
        valuesClone.remove('id')
        update(table, valuesClone, "`id`=?", id)
    }

    void update(String table, Map<String, Object> data, String where, Object... values){
        int ci = 0
        Object[] valuesArray = new Object[data.size()+values.length]
        List<String> sets = []
        for(key in data.keySet()){
            sets.add("`"+key+"`=?")
            valuesArray[ci] = data[key]
            ci++
        }
        for(int i=0; i<values.length; i++){
            valuesArray[ci] = values[i]
            ci++
        }
        try {
            PreparedStatement ps = setParams(connect().prepareStatement("UPDATE `"+table+"` SET "+String.join(',', sets)+(where!=null?" WHERE "+where+";":";")), valuesArray)
            ps.executeUpdate()
            ps.close()
        } catch (SQLException e) {e.printStackTrace()}
    }

    List<ORMEntry> select(String table, String where, String order, boolean desc, int limit, Object... values){
        List<ORMEntry> entries = []
        ResultSet rs = selectRaw("*", table, where, order, desc, limit, values)
        if(rs!=null){
            try {
                ResultSetMetaData meta = rs.getMetaData()
                while (rs.next()){
                    Map<String, Object> data = [:]
                    for(int i=0; i<meta.getColumnCount(); i++){
                        data[meta.getColumnName(i+1)] = rs.getObject(i+1)
                    }
                    entries.add(new ORMEntry(this, table, data))
                }
            }catch(SQLException ex){
                ex.printStackTrace()
            }
            statementMap[rs].close()
            statementMap.remove(rs)
        }
        entries
    }

    int count(String table, String where, String order, boolean desc, int limit, Object... values){
        ResultSet rs = selectRaw("COUNT(*)", table, where, order, desc, limit, values)
        int count = 0
        if(rs!=null){
            try {
                if(rs.next()){
                    count = rs.getInt(1)
                }
            }catch(SQLException ex){
                ex.printStackTrace()
            }
            statementMap[rs].close()
            statementMap.remove(rs)
        }
        count
    }

    private ResultSet selectRaw(String what, String table, String where, String order, boolean desc, int limit, Object... values){
        StringBuilder sb = new StringBuilder("SELECT "+what+" FROM `"+table+"`")
        if(where != null){
            sb.append(" WHERE ")
            sb.append(sqlifyNames(where))
        }
        if(order != null){
            sb.append(" ORDER BY `")
            sb.append(sqlifyName(order))
            sb.append('`')
            if(desc)
                sb.append(' DESC')
        }
        if(limit > 0){
            sb.append(" LIMIT "+limit)
        }
        sb.append(';')
        try {
            PreparedStatement ps = setParams(connect().prepareStatement(sb.toString()), values)
            ResultSet rs = ps.executeQuery()
            statementMap[rs] = ps
            return rs
        } catch (SQLException e) {e.printStackTrace()}
        null
    }

    private PreparedStatement setParams(PreparedStatement st, Object... parameters) throws SQLException {
        int i = 1
        for(Object object : parameters){
            Class type = object.getClass()
            if(type == String.class)
                st.setString(i,(String)object)
            else if(type == Integer.class)
                st.setInt(i,(Integer) object)
            else if(type == Double.class)
                st.setDouble(i,(Double) object)
            else if(type == Long.class)
                st.setLong(i,(Long) object)
            else if(type == Short.class)
                st.setShort(i,(Short) object)
            else if(type == Float.class)
                st.setFloat(i,(Float) object)
            else if(type == Timestamp.class)
                st.setTimestamp(i,(Timestamp) object)
            else if(type == java.sql.Date.class)
                st.setDate(i,(java.sql.Date) object)
            else if(type == Time.class)
                st.setTime(i,(Time)object)
            else
                System.out.println("[SQL] Could not set type: "+object.getClass().getName())
            i++
        }
        st
    }

    static String sqlifyName(String name){
        StringBuilder sb = new StringBuilder()
        for(int i=0; i<name.length(); i++){
            if(name.charAt(i).isUpperCase()){
                sb.append('_')
                sb.append(name.charAt(i).toLowerCase())
            }else{
                sb.append(name.charAt(i))
            }
        }
        sb.toString()
    }

    static String sqlifyNames(String name){
        StringBuilder sb = new StringBuilder()
        StringBuilder nameBuilder = null
        for(int i=0; i<name.length(); i++){
            if(nameBuilder != null){
                if(name.charAt(i) == '`' as char){
                    sb.append('`')
                    sb.append(sqlifyName(nameBuilder.toString()))
                    sb.append('`')
                    nameBuilder = null
                }else{
                    nameBuilder.append(name.charAt(i))
                }
            }else{
                if(name.charAt(i) == '`' as char){
                    nameBuilder = new StringBuilder()
                }else{
                    sb.append(name.charAt(i))
                }
            }
        }
        sb.toString()
    }

    def propertyMissing(String name){
        getAt(name)
    }

    def getAt(String name){
        new ORMQuery(this, name)
    }

}

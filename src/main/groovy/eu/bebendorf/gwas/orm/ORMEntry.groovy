package eu.bebendorf.gwas.orm

class ORMEntry {
    private ORMDatabase database
    private String table
    private Map<String, Object> old
    private Map<String, Object> columns
    ORMEntry(ORMDatabase database, String table, Map<String, Object> columns){
        this.database = database
        this.table = table
        this.old = (Map<String, Object>) columns.clone()
        this.columns = columns
    }
    def propertyMissing(String name, Object value){
        this[name] = value
    }
    def propertyMissing(String name){
        this[name]
    }
    def getAt(String key){
        String k = ORMDatabase.sqlifyName(key)
        if (!columns.containsKey(k)){
            if(columns.containsKey(k+'_id')){
                return database[k+'s'].id((Integer) columns[k+'_id'])
            }
            if(k.endsWith('s')){
                return database[k].where('`'+table.substring(0, table.length()-1)+'_id`=?', columns.id).all()
            }
        }
        return columns[ORMDatabase.sqlifyName(key)]
    }
    def putAt(String key, Object value){
        if(value instanceof ORMEntry){
            columns[ORMDatabase.sqlifyName(key)+'_id'] = value.id
            return null
        }
        columns[ORMDatabase.sqlifyName(key)] = value
    }
    void save(){
        Map<String, Object> data = [id: columns.id]
        for(key in columns.keySet()){
            if(old.containsKey(key) && old[key] == columns[key])
                continue
            old[key] = columns[key]
            data[key] = columns[key]
        }
        if(data.containsKey('id')){
            database.update(table, columns)
        }else{
            columns['id'] = database.insert(table, columns)
        }

    }
    void delete(){
        database.delete(table, "`id`=?", columns['id'])
        columns.remove('id')
        old = [:]
    }
}

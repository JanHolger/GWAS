package eu.bebendorf.gwas.orm

class ORMQuery {
    private ORMDatabase database
    private String table
    private String where = null
    private String order = null
    private boolean desc = false
    private int limit = 0
    private List<Object> values = new ArrayList<>()
    ORMQuery(ORMDatabase database, String table){
        this.database = database
        this.table = table
    }
    ORMEntry create(Map<String, Object> values){
        int id = database.insert(table, values)
        List<ORMEntry> entries = database.select(table, "`id`=?", null, false, 0, id)
        entries.size()>0?entries[0]:null
    }
    ORMQuery where(String query, Object... objects){
        this.where = query
        for(o in objects)
            values.add(o)
        return this
    }
    ORMQuery order(String col, boolean desc = false){
        this.order = col
        this.desc = desc
        return this
    }
    ORMQuery limit(int limit){
        this.limit = limit
        return this
    }
    void delete(){
        database.delete(table, where, values.toArray())
    }
    List<ORMEntry> all(){
        return database.select(table, where, order, desc, limit, values.toArray())
    }
    void each(Closure closure){
        all().each closure
    }
    ORMEntry id(int id){
        return where('`id`=?', id).one()
    }
    ORMEntry one(){
        List<ORMEntry> entries = all()
        entries.size()>0?entries[0]:null
    }
    ORMEntry oneOrCreate(Map<String, Object> data){
        one()?:create(data)
    }
    int count(){
        return database.count(table, where, order, desc, limit, values.toArray())
    }
    def propertyMissing(String name){
        if(name == "count"){
            return count()
        }
        if(name == "all"){
            return all()
        }
        if(name == "one"){
            return one()
        }
        return null
    }
}

package eu.bebendorf.gwas.router

abstract class RouteParamTransformer {
    private Map<String, String> regex = new HashMap<>()
    private Map<String, Closure> transformers = new HashMap<>()
    protected void add(String name, String regex, Closure transformer){
        if(name.contains("|")){
            for(s in name.split("\\|")){
                if(s.length() == 0)
                    continue
                add(s, regex, transformer)
            }
            return
        }
        this.regex[name] = regex
        transformers[name] = transformer
    }
    protected void extend(String parent, String name, Closure transformer){
        extend(this, parent, name, transformer)
    }
    protected void extend(RouteParamTransformerProvider parentTransformerProvider, String parent, String name, Closure transformer){
        extend(parentTransformerProvider.getRouteParamTransformer(parent), parent, name, transformer)
    }
    protected void extend(RouteParamTransformer parentTransformer, String parent, String name, Closure transformer){
        add(name, regex(parent), {
            String s -> transformer(parentTransformer.transform(parent, s))
        })
    }
    boolean canTransform(String name){
        return regex.containsKey(name)
    }
    String regex(String name){
        return regex[name]
    }
    Object transform(String name, String source){
        if(transformers.containsKey(name)){
            return transformers[name](source)
        }
        return source
    }
}
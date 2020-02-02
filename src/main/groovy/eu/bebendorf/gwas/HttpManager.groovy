package eu.bebendorf.gwas

import eu.bebendorf.gwas.helper.HttpMethod
import eu.bebendorf.gwas.orm.ORMDatabase
import eu.bebendorf.gwas.router.DefaultRouteParamTransformer
import eu.bebendorf.gwas.router.Route
import eu.bebendorf.gwas.router.RouteParamTransformer
import eu.bebendorf.gwas.router.RouteParamTransformerProvider
import eu.bebendorf.gwas.template.FileTemplateProvider
import eu.bebendorf.gwas.template.TemplateProvider
import groovy.text.Template

class HttpManager implements RouteParamTransformerProvider {
    final List<RouteParamTransformer> routeParamTransformers = [new DefaultRouteParamTransformer()]
    private Map<File, Map<Route, Closure>> controllers = [:]
    private File rootDirectory
    private File configFile
    private Map<File, Long> lastModified = [:]
    private File controllerDirectory
    private TemplateProvider templateProvider
    private StaticFileHandler staticFileHandler
    Map<String, ORMDatabase> databases = [:]

    HttpManager(File rootDirectory){
        this.rootDirectory = rootDirectory
        configFile = new File(rootDirectory, 'config.groovy')
        lastModified.put(configFile, 0)
        reloadConfig()
        this.controllerDirectory = new File(rootDirectory, 'controller')
        this.staticFileHandler = new StaticFileHandler(new File(rootDirectory, 'public'))
        this.templateProvider = new FileTemplateProvider(new File(rootDirectory, 'views'))
    }

    void reloadConfig(){
        if(lastModified[configFile] == configFile.lastModified())
            return
        databases.clear()
        Binding binding = new Binding()
        binding['db'] = { String name, Map<String, Object> settings ->
            databases[name] = new ORMDatabase(settings)
        }
        GroovyScriptEngine engine = new GroovyScriptEngine(rootDirectory.getPath())
        engine.run(configFile.getName(), binding)
        lastModified[configFile] = configFile.lastModified()
    }

    void reload(){
        controllers.keySet().each {
            if(!it.exists()){
                controllers.remove(it)
                lastModified.remove(it)
            }
        }
        loadFolder(controllerDirectory)
    }
    private void loadFolder(File file){
        if(file.isDirectory()){
            file.listFiles().each { loadFolder it }
            return
        }
        if(file.getName().endsWith(".groovy")){
            if(!controllers.containsKey(file) || lastModified[file] != file.lastModified()){
                GroovyScriptEngine scriptEngine = new GroovyScriptEngine(file.getAbsoluteFile().getParent())
                Map<Route, Closure> routes = [:]
                Binding binding = new Binding()
                Closure route = { HttpMethod method, String path, Closure closure -> routes.put(new Route(this, method, path), closure) }
                binding['route'] = route
                HttpMethod.values().each { binding[it.name().toLowerCase(Locale.ENGLISH)] = { String path, Closure closure -> route(it, path, closure) } }
                binding['view'] = { String name, Map model = [:] ->
                    model['view'] = binding['view']
                    Template template = templateProvider.get(name)
                    if(template != null){
                        return template.make(model)
                    }
                    null
                }
                binding['db'] = databases
                scriptEngine.run(file.getName(), binding)
                controllers.put(file, routes)
                lastModified.put(file, file.lastModified())
            }
        }
    }
    RouteParamTransformer getRouteParamTransformer(String type) {
        for(transformer in routeParamTransformers){
            if(transformer.canTransform(type)){
                return transformer
            }
        }
        null
    }
    boolean execute(Exchange exchange){
        reloadConfig()
        reload()
        for(file in controllers.keySet()){
            Map<Route, Closure> map = controllers[file]
            for(route in map.keySet()){
                Map<String, Object> res = route.match(exchange.method, exchange.path)
                if(res != null){
                    exchange.pathVariables = res
                    map[route].setProperty('exchange', exchange)
                    String result = map[route]()
                    if(result != null){
                        exchange.header('content-type', 'text/html')
                        exchange.write(result)
                    }
                    exchange.close()
                    return true
                }
            }
        }
        return staticFileHandler.execute(exchange)
    }
}

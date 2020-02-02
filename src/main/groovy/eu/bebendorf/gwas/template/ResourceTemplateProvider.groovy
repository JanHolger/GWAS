package eu.bebendorf.gwas.template

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine

class ResourceTemplateProvider implements TemplateProvider {
    private Map<String, Template> templateMap = [:]
    private ClassLoader classLoader
    private String directoryRoot
    private TemplateEngine templateEngine
    ResourceTemplateProvider(ClassLoader classLoader, String directoryRoot){
        this.classLoader = classLoader
        this.directoryRoot = directoryRoot.endsWith("/")?directoryRoot.substring(0, directoryRoot.length()-1):directoryRoot
        templateEngine = new SimpleTemplateEngine()
    }
    Template get(String name) {
        if(!name.startsWith('/'))
            name = '/' + name
        if(!name.endsWith('.gsp'))
            name += '.gsp'
        try {
            InputStream is = classLoader.getResourceAsStream(directoryRoot+name)
            templateMap[name] = templateEngine.createTemplate(new InputStreamReader(is))
        }catch(IOException ignored){}
        templateMap[name]
    }
}

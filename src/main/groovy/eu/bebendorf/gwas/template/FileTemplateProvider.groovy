package eu.bebendorf.gwas.template

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine

class FileTemplateProvider implements TemplateProvider {
    private Map<String, Template> templateMap = [:]
    private Map<String, Long> lastModified = [:]
    private File directoryRoot
    private TemplateEngine templateEngine
    FileTemplateProvider(File directoryRoot){
        this.directoryRoot = directoryRoot
        templateEngine = new SimpleTemplateEngine()
    }
    Template get(String name) {
        if(!name.endsWith('.gsp')){
            name += '.gsp'
        }
        File file = new File(directoryRoot, name)
        if(!file.exists()){
            templateMap.remove(name)
            lastModified.remove(name)
            return null
        }
        if(!templateMap.containsKey(name) || file.lastModified() != lastModified[name]){
            templateMap[name] = templateEngine.createTemplate(file)
            lastModified[name] = file.lastModified()
        }
        templateMap[name]
    }
}

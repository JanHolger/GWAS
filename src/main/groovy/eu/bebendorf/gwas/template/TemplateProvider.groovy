package eu.bebendorf.gwas.template

import groovy.text.Template

interface TemplateProvider {
    Template get(String name)
}
package eu.bebendorf.gwas.helper

import groovy.transform.TupleConstructor

@TupleConstructor
enum MimeType {
    HTML("text/html", ["html","htm"]),
    CSS("text/css", ["css"]),
    JS("application/javascript", ["js"]),
    JSON("application/json", ["json"]),
    PDF("application/pdf", ["pdf"]),
    XML("application/xml", ["xml"]),
    ZIP("application/zip", ["zip"]),
    PNG("image/png", ["png"]),
    JPG("image/jpeg", ["jpg", "jpeg"]),
    SVG("image/svg+xml", ["svg"]),
    ICO("image/x-icon", ["ico"]),
    PLAIN("text/plain", ["txt"]),
    MP4("video/mp4", ["mp4"]),
    MP3("audio/mpeg", ["mp3"]),
    WAV("audio/wav", ["wav"]),
    OGG("audio/ogg", ["ogg"])
    final String mime
    final List<String> extensions
    static MimeType byExtension(String extension){
        if(extension.startsWith("."))
            extension = extension.substring(1)
        for(type in values()){
            if(type.extensions.contains(extension)){
                return type
            }
        }
        return null
    }
    static MimeType byFileName(String fileName){
        if(fileName.contains("/"))
            fileName = fileName.split("/").last()
        if(!fileName.contains("."))
            return PLAIN
        MimeType type = byExtension(fileName.split("\\.").last())
        return type?:PLAIN
    }
}
package eu.bebendorf.gwas

import eu.bebendorf.gwas.helper.HttpMethod
import eu.bebendorf.gwas.helper.MimeType

class StaticFileHandler {

    private File rootDirectory

    StaticFileHandler(File rootDirectory){
        this.rootDirectory = rootDirectory
    }

    boolean execute(Exchange exchange){
        if(exchange.getMethod() != HttpMethod.GET)
            return false
        File file = new File(rootDirectory, exchange.getPath())
        if(!file.exists())
            return false
        MimeType mimeType = MimeType.byFileName(file.getName())
        exchange.header("content-type", mimeType.mime)
        FileInputStream fis = new FileInputStream(file)
        while (fis.available() > 0){
            byte[] bytes = new byte[1024]
            int r = fis.read(bytes)
            exchange.write(bytes, 0, r)
        }
        exchange.close()
        return true
    }

}

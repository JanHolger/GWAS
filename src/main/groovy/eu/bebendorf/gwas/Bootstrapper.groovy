package eu.bebendorf.gwas

import org.eclipse.jetty.server.Server

class Bootstrapper {

    static void main(String[] args){
        File appFolder = null
        if(args.length>0){
            appFolder = new File(args[0])
            if(!appFolder.exists())
                appFolder = null
        }
        if(appFolder == null)
            appFolder = new File('htdocs')
        HttpManager httpManager = new HttpManager(appFolder)
        Server server = new Server(8080)
        server.setHandler(new HttpHandler(httpManager))
        server.start()
        server.join()
    }

}

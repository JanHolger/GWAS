package eu.bebendorf.gwas

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpHandler extends AbstractHandler {
    private HttpManager httpManager
    HttpHandler(HttpManager httpManager){
        this.httpManager = httpManager
    }
    void handle(String path, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        httpManager.execute(new Exchange(path, httpServletRequest, httpServletResponse))
    }
}

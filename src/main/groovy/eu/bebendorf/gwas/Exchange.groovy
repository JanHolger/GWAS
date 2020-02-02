package eu.bebendorf.gwas

import eu.bebendorf.gwas.helper.HttpMethod

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

class Exchange {
    final HttpMethod method
    final String path
    Map<String, Object> pathVariables
    private HttpServletRequest request
    private HttpServletResponse response
    Exchange(String path, HttpServletRequest request, HttpServletResponse response){
        this.request = request
        this.response = response
        this.path = path
        method = HttpMethod.valueOf(request.getMethod())
    }
    void write(String data){
        write(data.getBytes(StandardCharsets.UTF_8))
    }
    void write(byte[] bytes){
        response.getOutputStream().write(bytes)
    }
    void write(byte[] bytes, int offset, int length){
        response.getOutputStream().write(bytes, offset, length)
    }
    void close(){
        response.getOutputStream().close()
    }
    void header(String header, String value){
        if(header.equalsIgnoreCase('content-type')){
            response.setContentType(value)
            return
        }
        response.setHeader(header, value)
    }
    String header(String header){
        request.getHeader(header)
    }
}
package net.flexberry.services.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
 


import org.apache.commons.io.IOUtils;
 
public class LoggerFilter implements Filter {
  @SuppressWarnings("unused")
  private FilterConfig config = null; 
  private boolean active = false; 
 
  public void destroy() {
    config = null;
  }
 
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (active){
      ResettableStreamHttpServletRequest wrappedRequest = new ResettableStreamHttpServletRequest(
          (HttpServletRequest) request);
      // wrappedRequest.getInputStream().read();
      String body = IOUtils.toString(wrappedRequest.getReader());
      System.err.println(body);
      //auditor.audit(wrappedRequest.getRequestURI(),wrappedRequest.getUserPrincipal(), body);
      wrappedRequest.resetInputStream();
      chain.doFilter(wrappedRequest, response);
    }else{
      chain.doFilter(request, response);
    }
  }
 
  public void init(FilterConfig config) throws ServletException {
    this.config = config; 
    String act = config.getInitParameter("active"); 
    if (act != null) {
      active = (act.toUpperCase().equals("TRUE"));
    } 
  }
 
  private static class ResettableStreamHttpServletRequest extends
      HttpServletRequestWrapper {
 
    private byte[] rawData;
    private HttpServletRequest request;
    private ResettableServletInputStream servletStream;
 
    public ResettableStreamHttpServletRequest(HttpServletRequest request) {
      super(request);
      this.request = request;
      this.servletStream = new ResettableServletInputStream();
    }
 
 
    public void resetInputStream() {
      servletStream.stream = new ByteArrayInputStream(rawData);
    }
 
    @Override
    public ServletInputStream getInputStream() throws IOException {
      if (rawData == null) {
        rawData = IOUtils.toByteArray(this.request.getReader());
        servletStream.stream = new ByteArrayInputStream(rawData);
      }
      return servletStream;
    }
 
    @Override
    public BufferedReader getReader() throws IOException {
      if (rawData == null) {
        rawData = IOUtils.toByteArray(this.request.getReader());
        servletStream.stream = new ByteArrayInputStream(rawData);
      }
      return new BufferedReader(new InputStreamReader(servletStream));
    }
    
 
    private class ResettableServletInputStream extends ServletInputStream {
 
      private InputStream stream;
 
      @Override
      public int read() throws IOException {
        return stream.read();
      }
    }
  }
 
}
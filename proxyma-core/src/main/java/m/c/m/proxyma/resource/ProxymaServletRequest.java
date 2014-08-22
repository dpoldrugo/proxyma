package m.c.m.proxyma.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * This class implements a requests wrapper.
 * It adapts servlet container requests to the Proxyma API.
 * Through this class Proxyma can transparently handle any Servlet request.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaServletRequest.java 176 2010-07-03 09:02:14Z marcolinuz $
 */
public class ProxymaServletRequest implements ProxymaRequest {
    /**
     * Default constructor for this class.
     * It thakes the original servlet request to wrap as parameter.
     * @param aRequest the request to satisfy
     * @param theContext the context where the request will live.
     */
    public ProxymaServletRequest (HttpServletRequest aRequest, ProxymaContext theContext) {
        this.theOriginalRequest = aRequest;
        this.log = theContext.getLogger();
    }

    /**
     * Returns an enumeration of all the header names this request contains.
     * If the request has no headers, this method returns an empty enumeration.
     * @return a collection of header names.
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        return theOriginalRequest.getHeaderNames();
    }

    /**
     * Returns the value of the specified request header as a String.
     * If the named header wasn't sent with the request, this method returns null.
     * The header name is case insensitive. You can use this method with any request header.
     *
     * @return the header value
     */
    @Override
    public String getHeader(String headerName) {
        return theOriginalRequest.getHeader(headerName);
    }

    /**
     * Returns all the values of the specified request header as an Enumeration of String objects.
     * Some headers, such as Accept-Language can be sent by clients as several headers each with
     * a different value rather than sending the header as a comma separated list.
     * If the request did not include any headers of the specified name, this method returns an empty Enumeration.
     * The header name is case insensitive. You can use this method with any request header.
     *
     * @return an Enumeration containing the values of the requested header.
     * If the request does not have any headers of that name return an empty enumeration.
     */
    @Override
    public Enumeration getHeaders(String headerName) {
        return theOriginalRequest.getHeaders(headerName);
    };

    /**
     * Returns the base path of proxyma from the request (without host, port, protocol and query string)
     * @return the absolute URI path that implements the current context.
     */
    @Override
    public String getContextURLPath() {
        String contextPath =  null;
        if ((theOriginalRequest.getServletPath() == null) || "".equals(theOriginalRequest.getServletPath()))
            contextPath =  theOriginalRequest.getContextPath();
        else {
            contextPath = theOriginalRequest.getContextPath() + theOriginalRequest.getServletPath();
        }
        return contextPath;
    }

    /**
     * Returns the path of the request (without protocol, host, port, proxyma context and query string)
     * @return the relative path from the proxyma base uri.
     */
    @Override
    public String getSubPath() {
        return theOriginalRequest.getPathInfo();
    }

    /**
     * Get the absoute requested path (without protocol, host, port and query string)
     * @return the absolute path of the requested URI.
     */
    @Override
    public String getRequestURI() {
        return theOriginalRequest.getRequestURI();
    }

    /**
     * Returns the query string that is contained in the request URL after the path.
     * This method returns null  if the URL does not have a query string.
     * @return the query string
     */
    @Override
    public String getQueryString() {
        return theOriginalRequest.getQueryString();
    }

    /**
     * Returns the name of the HTTP method with which this request was made,
     * for example, GET, POST, or PUT.
     * @return GET, POST, PUT...
     */
    @Override
    public String getMethod() {
        return theOriginalRequest.getMethod();
    }

    /**
     * Returns the value of the asked parameter
     * @param parameterName The namwe of the reqested paramenter
     * @return its value or null if it doesn't exists.
     */
    @Override
    public String getParameter(String parameterName) {
        return theOriginalRequest.getParameter(parameterName);
    }

    /**
     * Returns the names of the parameters into the request
     * @return the parameter names.
     */
    @Override
    public Enumeration getParameterNames() {
        return theOriginalRequest.getParameterNames();
    }

    /**
     * Returns the name and version of the protocol the request uses in the
     * form protocol/majorVersion.minorVersion, for example, HTTP/1.1.
     * @return the protocol
     */
    @Override
    public String getProtocol() {
        return theOriginalRequest.getProtocol();
    }

    /**
     * Returns the MIME type of the content of the request, or null if the type is not known.
     * @return the value of the content-type header.
     */
    @Override
    public String getContentType() {
        return theOriginalRequest.getContentType();
    }

    /**
     * Returns the name of the character encoding style used in this request.
     * This method returns null if the request does not use character encoding.
     * @return the utf-8, iso-8859-1...
     */
    @Override
    public String getCharacterEncoding() {
        return theOriginalRequest.getCharacterEncoding();
    }

    /**
     * Returns an array containing all of the Cookie  objects the browser sent with this request.
     * This method returns null if the browser did not send any cookies.
     * @return an array of cookies
     */
    @Override
    public Cookie[] getCookies() {
        return theOriginalRequest.getCookies();
    }

    /**
     * Returns the input stream for read the request's data sent by the client
     * @return the request InputStream.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return theOriginalRequest.getInputStream();
    }

    /**
     * Returns the host name of the server that received the request.
     * @return the server host name.
     */
    @Override
    public String getServerName() {
        return theOriginalRequest.getServerName();
    }

    /**
     * Returns the port number on which this request was received.
     * @return the server port
     */
    @Override
    public int getServerPort() {
       return theOriginalRequest.getServerPort();
    }

    /**
     * Returns the sheme used by the client to send the request.
     * @return http, http or ftp
     */
    @Override
    public String getScheme() {
        return theOriginalRequest.getScheme();
    }

    /**
     * Returns the fully qualified name of the client that sent the request.
     * @return the client host name
     */
    @Override
    public String getRemoteHost() {
        return theOriginalRequest.getRemoteHost();
    }

    /**
     * Returns the Internet Protocol (IP) address of the client that sent the request.
     * @return the ip of the client
     */
    @Override
    public String getRemoteAddress() {
        return theOriginalRequest.getRemoteAddr();
    }

    /**
     * Returns the name of the user making this request, if the user has logged in using HTTP authentication.
     * This method returns null if the user login is not authenticated.
     * Whether the user name is sent with each subsequent request depends on the browser.
     * @return the name of the authenticated user
     */
    @Override
    public String getRemoteUser() {
        return theOriginalRequest.getRemoteUser();
    }

    /**
     * This is the original "wrapped" request
     */
    private HttpServletRequest theOriginalRequest = null;

    /**
     * The logger for this request
     */
    private Logger log = null;
}

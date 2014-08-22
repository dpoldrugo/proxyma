package m.c.m.proxyma.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.servlet.http.Cookie;

/**
 * <p>
 * This the "Adapter" interface that will be used by Proxyma to manage client
 * requests. Any concrete class that implements this interface can be used as
 * Client request by Proxyma.
 * Through this interface Proxyma can transparently handle  "Servlet" or
 * "Portlet" requests in the same way.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaRequest.java 176 2010-07-03 09:02:14Z marcolinuz $
 */
public interface ProxymaRequest {
    /**
     * Returns an enumeration of all the header names this request contains.
     * If the request has no headers, this method returns an empty enumeration.
     * @return a collection of header names.
     */
    public Enumeration<String> getHeaderNames();
    
    /**
     * Returns the value of the specified request header as a String.
     * If the named header wasn't sent with the request, this method returns null.
     * The header name is case insensitive. You can use this method with any request header.
     * 
     * @return the header value
     */
    public String getHeader(String headerName);

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
    public Enumeration getHeaders(String headerName);
    
    /**
     * Returns the base path of proxyma from the request (without host, port, protocol and query string)
     * @return the absolute URI path that implements the current context.
     */
    public String getContextURLPath();

    /**
     * Returns the path of the request (without protocol, host, port, proxyma context and query string)
     * @return the relative path from the proxyma base uri.
     */
    public String getSubPath();

    /**
     * Get the absoute requested path (without protocol, host, port and query string)
     * @return the absolute path of the requested URI.
     */
    public String getRequestURI();
    
    /**
     * Returns the query string that is contained in the request URL after the path.
     * This method returns null  if the URL does not have a query string.
     * @return the query string
     */
    public String getQueryString();
    
    /**
     * Returns the name of the HTTP method with which this request was made,
     * for example, GET, POST, or PUT.
     * @return GET, POST, PUT...
     */
    public String getMethod();

    /**
     * Returns the value of the asked parameter
     * @param parameterName The namwe of the reqested paramenter
     * @return its value or null if it doesn't exists.
     */
    public String getParameter(String parameterName);

    /**
     * Returns the names of the parameters into the request
     * @return the parameter names.
     */
    public Enumeration getParameterNames();

    /**
     * Returns the name and version of the protocol the request uses in the
     * form protocol/majorVersion.minorVersion, for example, HTTP/1.1.
     * @return the protocol
     */
    public String getProtocol();

    /**
     * Returns the MIME type of the content of the request, or null if the type is not known.
     * @return the value of the content-type header.
     */
    public String getContentType();

    /**
     * Returns the name of the character encoding style used in this request.
     * This method returns null if the request does not use character encoding.
     * @return the utf-8, iso-8859-1...
     */
    public String getCharacterEncoding();

    /**
     * Returns an array containing all of the Cookie  objects the browser sent with this request.
     * This method returns null if the browser did not send any cookies.
     * @return an array of cookies
     */
    public Cookie[] getCookies();

    /**
     * Returns the input stream for read the request's data sent by the client
     * @return the request InputStream.
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Returns the sheme used by the client to send the request.
     * @return http, http or ftp
     */
    public String getScheme();


    /**
     * Returns the host name of the server that received the request.
     * @return the server host name.
     */
    public String getServerName();

    /**
     * Returns the port number on which this request was received.
     * @return the server port
     */
    public int getServerPort();

    /**
     * Returns the fully qualified name of the client that sent the request.
     * @return the client host name
     */
    public String getRemoteHost();

    /**
     * Returns the Internet Protocol (IP) address of the client that sent the request.
     * @return the ip of the client
     */
    public String getRemoteAddress();

    /**
     * Returns the name of the user making this request, if the user has logged in using HTTP authentication.
     * This method returns null if the user login is not authenticated.
     * Whether the user name is sent with each subsequent request depends on the browser.
     * @return the name of the authenticated user
     */
    public String getRemoteUser();
}

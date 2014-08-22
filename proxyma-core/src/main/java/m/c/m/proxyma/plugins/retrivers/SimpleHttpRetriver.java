package m.c.m.proxyma.plugins.retrivers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * This plugin uses an URLConnection to retrive the remoteData form the real server
 * and build a ResponseResourceDataBean that can be managed by transformers and
 * serializers for further elaborations.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: SimpleHttpRetriver.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class SimpleHttpRetriver extends m.c.m.proxyma.plugins.retrivers.AbstractRetriver {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger.
     *
     * NOTE: Every plugin to work must have a constructor that takes a ProxymaContext as parameter.
     */
    public SimpleHttpRetriver(ProxymaContext context) {
        //initialize the logger
        log = context.getLogger();
    }

    /**
     * This method register the time when it runs into a resource attribute.
     * @param aResource any ProxymaResource
     * @see AbstractRetriver for more informations
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception {
        HttpURLConnection conn = connectToServer(aResource);
        if (conn != null) {
            try {
                ProxymaResponseDataBean remoteData = createResponseDataBeanFromServerReply(conn, aResource);
                aResource.getResponse().setResponseData(remoteData);
                aResource.addAttibute(ORIGINAL_RESPONSE_ATTRIBUTE, remoteData);
            } catch (IOException ex) {
               log.severe("Unable to get data from remote server.. abort");
               throw new IOException("Unable to get data from remote server.. abort");
            } finally {
                conn.disconnect();
            }
        } else {
            log.severe("Unable to get data from remote server.. abort");
            throw new IOException("Unable to get data from remote server.. abort");
        }
    }

    /**
     * Returns the name of the plugin.
     * @return the name of the plugin.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of what the plugin does..<br/>
     * You can use html tags into it.<br/>
     * The result of this method call can be used by any interface
     * to explain for what is the puropse of the plugin.
     *
     * @return a short description of the plugin
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }

    /**
     * This method makes the connection to the remote server and returns the
     * URLConnection ready to be read to the invoker.
     * @param aResource the resource countainig the request
     * @return the wanted Urlconnection
     */
    private HttpURLConnection connectToServer(ProxymaResource aResource) {
        ProxymaRequest originalRequest = aResource.getRequest();
        String destination = composeURL(aResource);
        log.finer("Target URL: " + destination);
        HttpURLConnection retVal = null;
        log.finer("Connecting to the remote server..");
        try {
            //Prepare the connection to the remote server
            URL theUrl = new URL(destination);
            retVal = (HttpURLConnection) theUrl.openConnection();

            //set some useful properties for the connection
            retVal.setDoInput(true);
            retVal.setUseCaches(false);
            retVal.setInstanceFollowRedirects(false);

            //Set the headers copying them form the original request
            log.fine("Sending headers..");
            Enumeration<String> headerNames = originalRequest.getHeaderNames();
            Enumeration<String> propertyValues = null;
            String propertyName = null;
            String propertyValue = null;
            while (headerNames.hasMoreElements()) {
                propertyName = headerNames.nextElement();
                if (HOST_HEADER.equalsIgnoreCase(propertyName)) {
                    String newHostHeader = theUrl.getHost();
                    if (theUrl.getPort() > 0)
                        newHostHeader = newHostHeader + ":" + theUrl.getPort();
                    retVal.addRequestProperty(HOST_HEADER, newHostHeader);
                }else if (!COOKIE_HEADER.equalsIgnoreCase(propertyName)) {
                    propertyValues = originalRequest.getHeaders(propertyName);
                    while (propertyValues.hasMoreElements()) {
                        propertyValue = propertyValues.nextElement();
                        log.finer("Sending header: " + propertyName + "=" + propertyValue);
                        retVal.addRequestProperty(propertyName, propertyValue);
                    }
                }
            }

            //Set the special header that is a de facto standard for identifying the
            //originating IP address of a client connecting to a web server through an HTTP proxy
            retVal.addRequestProperty(X_FORWARDED_FOR_HEADER, originalRequest.getRemoteAddress());

            //Set the cookies for the request (that are nothing but headers)
            log.fine("Sending Cookies..");
            Collection cookies = getRequestCookies(originalRequest);
            String serializedCookie = null;
            if ((cookies != null) && (cookies.size() > 0)) {
                Iterator iter = cookies.iterator();
                while (iter.hasNext()) {
                    serializedCookie = (String) iter.next();
                    log.finer("Sending Cookie: " + serializedCookie);
                    retVal.addRequestProperty(COOKIE_HEADER, serializedCookie);
                }
            }

            //get the connection method
            String method = originalRequest.getMethod();

            //set the body of the connection if needed..
            int maxUpstreamSize = aResource.getProxyFolder().getMaxPostSize();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                log.fine("Sending Data..");
                retVal.setDoOutput(true);

                BufferedInputStream reader = new BufferedInputStream(originalRequest.getInputStream());
                BufferedOutputStream writer = new BufferedOutputStream(retVal.getOutputStream());
                byte[] buffer = new byte[BUF_SIZE];
                int count;
                int upstreamSize = 0; //size of the POST remoteData
                while ((count = reader.read(buffer, 0, BUF_SIZE)) > -1) {
                    writer.write(buffer, 0, count);
                    upstreamSize += count;
                    if ((upstreamSize > maxUpstreamSize) && (maxUpstreamSize > 0)) {
                        log.warning("WARNING: Upstream size of data overrides the maximum allowed size for this folder ("
                                + maxUpstreamSize + " bytes).");
                        //cleanup..
                        writer.flush();
                        writer.close();
                        reader.close();
                        throw new IOException("Upstream size of data overrides the maximum allowed size for the proxy-folder");
                    }
                }

                //cleanup..
                writer.flush();
                writer.close();
                reader.close();
            }

            //set the connection method
            log.finer("Set Method: " + method);
            retVal.setRequestMethod(method);

        } catch (IOException ex) {
            log.severe("Unable to obtain dtata from: " + destination + "!!!");
            ex.printStackTrace();
            if (retVal != null) {
                retVal.disconnect();
                retVal = null;
            }
        }

        return retVal;
    }

    /**
     * Compose the URL form the request into the passed resource
     * @param aResource the resource countaining the request
     * @return the url to connect to.
     */
    private String composeURL(ProxymaResource aResource) {
        ProxymaRequest theRequest = aResource.getRequest();
        StringBuffer retVal = new StringBuffer(aResource.getProxyFolder().getDestinationAsString());
        retVal.append(aResource.getDestinationSubPath());

        if (theRequest.getQueryString() != null) {
            retVal.append("?").append(theRequest.getQueryString());
        }

        return retVal.toString();
    }

    /**
     * Decode the cookie objecs of the passed reqiuest and serialize them into
     * cookie headers that can be used into an urlconnection.
     * @param request the request countaining the cookies to serialize
     * @return a Collection of serilized cookies
     */
    private Collection<String> getRequestCookies(ProxymaRequest request) {
        LinkedList retValue = new LinkedList();
        Cookie[] cookies = request.getCookies();
        if ((cookies != null) && (cookies.length > 0)) {
            Cookie requestCookie;
            for (int loop = 0; loop < cookies.length; loop++) {
                StringBuffer currentCookie = new StringBuffer();
                requestCookie = cookies[loop];

                currentCookie.append(requestCookie.getName());
                currentCookie.append(COOKIE_VALUE_DELIMITER);
                currentCookie.append(requestCookie.getValue());

                String strVal;
                int intVal = requestCookie.getMaxAge();
                if (intVal > 0) {
                    long exp = new Date().getTime() + (intVal * 1000);
                    strVal = dateFormat.format(new Date(exp));
                    currentCookie.append(COOKIE_TOKENS_DELIMITER);
                    currentCookie.append(COOKIE_EXPIRES);
                    currentCookie.append(strVal);
                }
                strVal = requestCookie.getPath();
                if (strVal != null) {
                    currentCookie.append(COOKIE_TOKENS_DELIMITER);
                    currentCookie.append(COOKIE_PATH);
                    currentCookie.append(requestCookie.getPath());
                }
                strVal = requestCookie.getDomain();
                if (strVal != null) {
                    currentCookie.append(COOKIE_TOKENS_DELIMITER);
                    currentCookie.append(COOKIE_DOMAIN);
                    currentCookie.append(requestCookie.getDomain());
                }

                if (requestCookie.getSecure()) {
                    currentCookie.append(COOKIE_TOKENS_DELIMITER);
                    currentCookie.append(COOKIE_SECURE);
                }

                intVal = requestCookie.getVersion();
                if (intVal > 1) {
                    currentCookie.append(COOKIE_TOKENS_DELIMITER);
                    currentCookie.append(COOKIE_VERSION);
                    currentCookie.append(Integer.toString(intVal));
                }
                retValue.add(currentCookie.toString());
                
            }
        }

        return retValue;
    }

    /**
     * Parse all the values into the server response and build a new
     * ResponseDataBean that can be used by the other plugins and serializers.
     *
     * @param conn the connection to the server
     * @param aResurce the resource to populate
     */
    private ProxymaResponseDataBean createResponseDataBeanFromServerReply(HttpURLConnection urlConnection, ProxymaResource aResurce) throws IOException {
        log.fine("Connection succeded, buffering resoponse data..");

        ProxymaResponseDataBean responseData = new ProxymaResponseDataBean();

        //First of all, set the HTTP status code:
        int statusCode = urlConnection.getResponseCode();
        log.finer("Setting response status to: " + statusCode);
        responseData.setStatus(statusCode);

        //Contents are always unpacked to allow plugins to inspect the content.
        //This flag is setted to true if the original content was gzipped.
        boolean packedData = false;

        //now get all the headers
        log.fine("Setting response headers and Cookies..");
        Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
        Iterator<String> headerNames = responseHeaders.keySet().iterator();
        String header = null;
        String value = null;
        Iterator<String> valuesList = null;
        while (headerNames.hasNext()) {
            header = headerNames.next();
            if (header == null) //jump the status record
                header = headerNames.next();
            valuesList = responseHeaders.get(header).iterator();
            while (valuesList.hasNext()) {
                value = valuesList.next();
                if (SET_COOKIE_HEADER.equalsIgnoreCase(header)) {
                    //process cookies one at a time..
                    log.finer("Processing Set-Cookie Header. Value=\"" + value + "\"");
                    StringTokenizer st = new StringTokenizer(value, COOKIE_TOKENS_DELIMITER);
                    Cookie theCookie = null;
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        String cookieNameValue[] = token.split(COOKIE_VALUE_DELIMITER);
                        if (cookieNameValue.length == 2)
                            theCookie = new Cookie(cookieNameValue[0], cookieNameValue[1].trim());
                        else
                            theCookie = new Cookie(cookieNameValue[0], EMPTY_STRING);
                    }
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (token.startsWith(COOKIE_DOMAIN)) {
                            //Rewrites the domain of cookie (this will be rewritten soon by the rewriter)
                            token = token.replaceFirst(COOKIE_DOMAIN, EMPTY_STRING);
                            theCookie.setDomain(token.trim());
                        } else if (token.startsWith(COOKIE_PATH)) {
                            //Rewrites the path of cookie (even this will be rewritten soon by the request forwarder)
                            token = token.replaceFirst(COOKIE_PATH, EMPTY_STRING);
                            theCookie.setPath(token.trim());
                        } else if (token.startsWith(COOKIE_EXPIRES)) {
                            //process expiration date of the cookie
                            token = token.replaceFirst(COOKIE_EXPIRES, EMPTY_STRING);
                            Date now = new Date();
                            try {
                                theCookie.setMaxAge(now.compareTo(dateFormat.parse(token)));
                            } catch (ParseException e) {
                                //do not set the age
                            }
                        } else if (token.startsWith(COOKIE_VERSION)) {
                            //process path of cookie (even this will be rewritten soon by the rewriter)
                            token = token.replaceFirst(COOKIE_VERSION, EMPTY_STRING);
                            theCookie.setVersion(Integer.parseInt(token.trim()));
                        } else if (token.startsWith(COOKIE_SECURE)) {
                            theCookie.setSecure(true);
                        }
                    }

                    log.finer("Setting Adding Cookie: " + theCookie.getName());
                    responseData.addCookie(theCookie);
                } else if (CONTENT_ENCODING_HEADER.equalsIgnoreCase(header) && (value.indexOf("gzip") >= 0)) {
                    packedData = true;
                    log.finer("Skipping content encoding Header (content unpacked)");
                } else if (TRANSFER_ENCODING_HEADER.equalsIgnoreCase(header)) {
                    log.finer("Skipping Transfer encoding Header (content unpacked)");
                } else {
                    log.finer("Adding header: " + header + "=" + value);
                    responseData.addHeader(header, value);
                }
            }
        }

        //Load all remoteData as binary into a ByteBuffer
        log.fine("Loading response data..");
        BufferedInputStream reader = null;
        try {
            ByteBuffer theBuffer = ByteBufferFactory.createNewByteBuffer(aResurce.getContext());
            if (packedData) {
                reader = new BufferedInputStream(new GZIPInputStream(urlConnection.getInputStream()));
            } else {
                reader = (new BufferedInputStream(urlConnection.getInputStream()));
            }

            int count;
            byte buffer[] = new byte[BUF_SIZE];
            while ((count = reader.read(buffer, 0, BUF_SIZE)) > -1) {
                theBuffer.appendBytes(buffer, count);
            }
            responseData.setData(theBuffer);
        } catch (FileNotFoundException x) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.fine("Resource retriving completed.");
        return responseData;
    }

    /**
     * The logger of the context..
     */
    private Logger log = null;

    /**
     * Size of the reading buffers.
     */
    private static final int BUF_SIZE = 1024; //1Kb
    
    /**
     * An empty string
     */
    private static final String EMPTY_STRING = "";

    /**
     * The standard content-type header
     */
    private final static String HOST_HEADER = "Host";

    /**
     * The standard content-encoding header.
     */
    public final static String CONTENT_ENCODING_HEADER = "Content-Encoding";

    /**
     * The standard Trensfer Encodign Header
     */
    public final static String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";

    /**
     * The standard name of the cookies header
     */
    private final static String COOKIE_HEADER = "Cookie";

    /**
     * The standard name of the set-cookies header
     */
    private final static String SET_COOKIE_HEADER = "Set-Cookie";

    /**
     * The cookie declared path
     */
    private final static String COOKIE_PATH = "path=";

    /**
     * the cookie declared domain (or host)
     */
    private final static String COOKIE_DOMAIN = "domain=";

    /**
     * The cookie expiration time
     */
    private final static String COOKIE_EXPIRES = "expires=";

    /**
     * The cookie version
     */
    private final static String COOKIE_VERSION = "version=";

    /**
     * The cookie declare to be secure
     */
    private final static String COOKIE_SECURE = "secure";

    /**
     * The cookie tokens standard delimiter
     */
    private final static String COOKIE_TOKENS_DELIMITER = ";";

    /**
     * The cookie values standard delimiter
     */
    private final static String COOKIE_VALUE_DELIMITER = "=";

    /**
     * The standard date format for the cookies.
     */
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");

    /**
     * The special header that tells to the real servers for who the proxy is working
     */
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    /**
     * Attribute setted by this plugin into the resource to allof further inspections
     * on the original retrived tata from subsequent plugins.
     */
    private static final String ORIGINAL_RESPONSE_ATTRIBUTE = "Original-Response";

    /**
     * The name of this plugin.
     */
    private static final String name = "Simple Http Retriver";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This plugin retrives the requested data from the remote " +
            "hosts (real servers) using the HTTP Protocol.<br/>" +
            "Note for developers: it also writes into the resource an attribute (Original-Response) that countains " +
            "the original retrived response.";
}

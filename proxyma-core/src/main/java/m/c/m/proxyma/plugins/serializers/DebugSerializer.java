package m.c.m.proxyma.plugins.serializers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * This plugin is a simple text serializer.<br/>
 * Its work is to dump all the data of the request, of the original response
 * and of the transformed response in text mode.<br/>
 * NOTE: It oesn't writes the "access log" entries because it's supposed to be
 * a debug plugin."
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: DebugSerializer.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class DebugSerializer extends m.c.m.proxyma.plugins.serializers.AbstractSerializer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public DebugSerializer (ProxymaContext context) {
        //initialize the context logget
        log = context.getLogger();
    }

    /**
     * This method sends back to the client the response-data of the resource
     * adding to it only some useful headers.<br/>
     * It writes also the access log records in common logging format.
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception{
        //Get the response data from the resource
        ProxymaRequest request = aResource.getRequest();
        ProxymaResponseDataBean original = (ProxymaResponseDataBean)aResource.getAttribute(ORIGINAL_RESPONSE_ATTRIBUTE);
        ProxymaResponseDataBean processed = aResource.getResponse().getResponseData();

        //First of all creates a new responseDataBean for the new Response
        log.fine("Generating debug page..");
        ProxymaResponseDataBean debugPage = new ProxymaResponseDataBean();
        String charsetEncoding = aResource.getContext().getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING);

        //Set response status
        debugPage.setStatus(STATUS_OK);

        //Add Date header
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        debugPage.addHeader(DATE_HEADER, format.format(new Date()));

        //Add Server header
        debugPage.addHeader(SERVER_HEADER, aResource.getContext().getProxymaVersion());

        //Add Content-Type header
        debugPage.addHeader(CONTENT_TYPE_HEADER, "text/plain;charset="+charsetEncoding);

        //Add headers for the cache providers to avoid to cache this resource
        debugPage.addHeader(CACHE_CONTROL_HEADER, NO_CACHE);
        debugPage.addHeader(PRAGMA_HEADER, NO_CACHE);

        //Prepare the byte buffer for the page content.
        ByteBuffer out = ByteBufferFactory.createNewByteBuffer(aResource.getContext());

        //Start dumping page header
        byte[] line = null;
        String now = pageHeaderDateFormat.format(new Date());
        line = ("***************  PROXYMA DUMP " + now + "  ***************\n").getBytes(charsetEncoding);
        out.appendBytes(line, line.length);

        //Start writing client request data
        log.finer("dumping request data..");
        if (request != null) {
            log.finer("dumping client request data..");
            line = ("\nCLIENT REQUEST DATA\n").getBytes(charsetEncoding);
            out.appendBytes(line, line.length);
            dumpRequestData(request, out, charsetEncoding, aResource);
        }

        //Write the original response data
        if (original != null) {
            log.finer("dumping original response data..");
            line = ("\nORIGINAL RESPONSE DATA\n").getBytes(charsetEncoding);
            out.appendBytes(line, line.length);
            dumpResponseData(original, out, charsetEncoding);
        }

        //Check If there was a Cache Hit.
        if (aResource.getAttribute(CACHE_HIT_ATTRIBUTE) != null) {
            log.finer("Cache Hit for this resource..");
            line = ("\n+++++++++++++++++++++++++++\n++++++ CACHE HIT!!! ++++++\n+++++++++++++++++++++++++++\n").getBytes(charsetEncoding);
            out.appendBytes(line, line.length);
        }


        //Write processed response data
        if (processed != null) {
            log.finer("dumping output response data..");
            line = ("\nPROCESSED RESPONSE DATA\n").getBytes(charsetEncoding);
            out.appendBytes(line, line.length);
            dumpResponseData(processed, out, charsetEncoding);
        }

        //mark the end of the dump.
        line = ("*************************  PROXYMA DUMP END  *************************\n").getBytes(charsetEncoding);
        out.appendBytes(line, line.length);

        //add the buffer to the response
        debugPage.setData(out);

        //add the response data to the resource
        aResource.getResponse().setResponseData(debugPage);

        log.finer("Debug page generation completed.");
      
        //Send the resource back to the client
        aResource.getResponse().sendDataToClient();
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
     * Dump in text format the data of the passed response into the specified
     * buffer using the provided encoding.
     *
     * @param response the response to dump
     * @param output the buffer to fill
     * @param encoding the encoding to use for the dump
     * 
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    private void dumpResponseData (ProxymaResponseDataBean response, ByteBuffer output, String encoding)
                                   throws UnsupportedEncodingException, IOException, IllegalArgumentException,
                                          ClassNotFoundException, IllegalAccessException, InstantiationException,
                                          InvocationTargetException {
        byte[] line = null;
        String now = pageHeaderDateFormat.format(new Date());
        line = ("\nStatus: " + response.getStatus() + "\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        line = ("\nHeaders:\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        Iterator<String> stringIterarot = response.getHeaderNames().iterator();
        String headerName = null;
        ProxymaHttpHeader header = null;
        Collection<ProxymaHttpHeader> multiHeader = null;
        while (stringIterarot.hasNext()) {
            headerName = stringIterarot.next();
            if (response.isMultipleHeader(headerName)) {
                //Process multiple values header.
                multiHeader = response.getMultivalueHeader(headerName);
                Iterator<ProxymaHttpHeader> headers = multiHeader.iterator();
                while (headers.hasNext()) {
                    header = headers.next();
                    line = ("\t" + header.getName() + ": " + header.getValue() + "\n").getBytes(encoding);
                    output.appendBytes(line, line.length);
                }
            } else {
                //Process Sungle value header
                header = response.getHeader(headerName);
                line = ("\t" + header.getName() + ": " + header.getValue() + "\n").getBytes(encoding);
                output.appendBytes(line, line.length);
            }
        }

        line = ("\nCookies:\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        Iterator<Cookie> cookieIterator = response.getCookies().iterator();
        while (cookieIterator.hasNext()) {
            Cookie cookie = cookieIterator.next();
            line = ("\t" + SET_COOKIE_HEADER + ": " + serializeCookie(cookie) + "\n").getBytes(encoding);
            output.appendBytes(line, line.length);
        }

        line = ("\nRaw Data:\n----8<----8<----8<----8<----8<----8<----\n").getBytes(encoding);
        output.appendBytes(line, line.length);

         if (response.getData() != null) {
            ByteBufferReader data = ByteBufferFactory.createNewByteBufferReader(response.getData());
            byte[] buffer = new byte[WRITE_BUFFER_SIZE];
            int count;
            while ((count = data.readBytes(buffer, WRITE_BUFFER_SIZE)) >= 0)
                output.appendBytes(buffer, count);
        }

        line = ("\n----8<----8<----8<----8<----8<----8<----\n\n\n").getBytes(encoding);
        output.appendBytes(line, line.length);
    }


    /**
     * Dump in text format the request data into the provided buffer and using
     * the specified encoding.
     *
     * @param request the original request
     * @param output the byteBuffer the will be the output of the serialization
     * @param encoding the encoding for the text
     * @param aResource the current resource
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void dumpRequestData(ProxymaRequest request, ByteBuffer output, String encoding, ProxymaResource aResource)
                                throws UnsupportedEncodingException, IOException {

        ProxymaRequest theRequest = aResource.getRequest();
        StringBuffer requestedURL = new StringBuffer(aResource.getProxyFolder().getDestinationAsString());
        requestedURL.append(aResource.getDestinationSubPath());
        byte[] line = null;
        
        if (theRequest.getQueryString() != null) {
            requestedURL.append("?").append(theRequest.getQueryString());
        }

        line = ("\nMethod: " + request.getMethod() + "\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        line = ("\nURL: " + requestedURL.toString() + "\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        line = ("\nHeaders:\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        Enumeration<String> headerNames = request.getHeaderNames();
        Enumeration<String> propertyValues = null;
        String headerName = null;
        String headerValue = null;
        while (headerNames.hasMoreElements()) {
            headerName = headerNames.nextElement();
            if (!COOKIE_HEADER.equalsIgnoreCase(headerName)) {
                propertyValues = request.getHeaders(headerName);
                while (propertyValues.hasMoreElements()) {
                    headerValue = propertyValues.nextElement();
                    line = ("\t" + headerName + ": " + headerValue + "\n").getBytes(encoding);
                    output.appendBytes(line, line.length);
                }
            }
        }

        line = ("\nCookies:\n").getBytes(encoding);
        output.appendBytes(line, line.length);

        Cookie[] cookies = request.getCookies();
        if ((cookies != null) && (cookies.length > 0)) {
            for (int loop = 0; loop < cookies.length; loop++) {
                String serializedCookie = serializeCookie(cookies[loop]);
                line = ("\t" + COOKIE_HEADER + ": " + serializedCookie + "\n").getBytes(encoding);
                output.appendBytes(line, line.length);
            }
        }

        line = ("\n******************************************************\n").getBytes(encoding);
        output.appendBytes(line, line.length);
    }

    /**
     * Serialize the passed cookie into a convenient string format.
     * @param aCookie the coookie to serialize
     * @return the string version of the cookie.
     */
    private String serializeCookie(Cookie aCookie) {
        StringBuffer currentCookie = new StringBuffer();

        currentCookie.append(aCookie.getName());
        currentCookie.append(COOKIE_VALUE_DELIMITER);
        currentCookie.append(aCookie.getValue());

        String strVal;
        int intVal = aCookie.getMaxAge();
        if (intVal > 0) {
            long exp = new Date().getTime() + (intVal * 1000);
            strVal = cookieDateFormat.format(new Date(exp));
            currentCookie.append(COOKIE_TOKENS_DELIMITER);
            currentCookie.append(COOKIE_EXPIRES);
            currentCookie.append(strVal);
        }
        strVal = aCookie.getPath();
        if (strVal != null) {
            currentCookie.append(COOKIE_TOKENS_DELIMITER);
            currentCookie.append(COOKIE_PATH);
            currentCookie.append(aCookie.getPath());
        }
        strVal = aCookie.getDomain();
        if (strVal != null) {
            currentCookie.append(COOKIE_TOKENS_DELIMITER);
            currentCookie.append(COOKIE_DOMAIN);
            currentCookie.append(aCookie.getDomain());
        }

        if (aCookie.getSecure()) {
            currentCookie.append(COOKIE_TOKENS_DELIMITER);
            currentCookie.append(COOKIE_SECURE);
        }

        intVal = aCookie.getVersion();
        if (intVal > 1) {
            currentCookie.append(COOKIE_TOKENS_DELIMITER);
            currentCookie.append(COOKIE_VERSION);
            currentCookie.append(Integer.toString(intVal));
        }

        return currentCookie.toString();
    }

    /**
     * The logger of the context..
     */
    private Logger log = null;

    /**
     * The date formatter for the common logging format.
     */
    private final Format pageHeaderDateFormat = new SimpleDateFormat("[dd/MMM/yyyy - HH:mm:ss Z]");

    /**
     * The standard date format for the cookies.
     */
    private final static SimpleDateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");

    /**
     * Attribute setted by this plugin into the resource to allof further inspections
     * on the original retrived tata from subsequent plugins.
     */
    private static final String ORIGINAL_RESPONSE_ATTRIBUTE = "Original-Response";

    /**
     * The attribute name that will be stored into the resource
     * on every cache-hit.
     */
    private static final String CACHE_HIT_ATTRIBUTE = "Cache-Hit";


     /**
     * Standard status code for a successfull response
     */
    private static final int STATUS_OK = 200;

    /**
     * The date header name
     */
    private static final String DATE_HEADER = "Date";

    /**
     * The originating server header name
     */
    private static final String SERVER_HEADER = "Server";

    /**
     * The content typp / encoding  header name
     */
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * One of the headers that controls the behavior of the caches
     */
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    /**
     * One of the headers that controls the behavior of the caches
     */
    private static final String PRAGMA_HEADER = "Pragma";

    /**
     * Directive to avoid chaching
     */
    private static final String NO_CACHE = "no-cache";

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
     * Size of the buffer to write data into the http response.
     */
    private static final int WRITE_BUFFER_SIZE = 1024; //these are bytes (1Kb)

    /**
     * The name of this plugin.
     */
    private static final String name = "Debug Serializer";


    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is a text/plain serializer.<br/>"
            + "Its purpose is to dump all the data processed by the proxy engine.<br/>"
            + "In other words, it will dump in text format all the request data, the original-server response "
            + "and the transformed respone.<br/>Note: No access log records are written when this plugin is active.";
}

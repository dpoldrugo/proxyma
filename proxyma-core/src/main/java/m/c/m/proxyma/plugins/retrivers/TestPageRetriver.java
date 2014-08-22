package m.c.m.proxyma.plugins.retrivers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * This retriver simply generates a test page.<br/>
 * It's useful only for testing puropses.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: TestPageRetriver.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class TestPageRetriver extends m.c.m.proxyma.plugins.retrivers.AbstractRetriver {
    /**
     * The default constructor for this class<br/>
     * It prepares the context logger.
     *
     * NOTE: Every plugin to work must have a constructor that takes a ProxymaContext as parameter.
     */
    public TestPageRetriver (ProxymaContext context) {
        //initialize the logger
        log = context.getLogger();
    }

    /**
     * This method creates a test page to sent back to the client.
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception {
        log.info("Generating test page for the retriver..");

        ProxymaResponseDataBean listPage = new ProxymaResponseDataBean();
        String charsetEncoding = aResource.getContext().getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING);

        log.fine("Generating Test page..");

        //Set response status
        listPage.setStatus(STATUS_OK);

        //Add Date header
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        listPage.addHeader(DATE_HEADER, format.format(new Date()));

        //Add Server header
        listPage.addHeader(SERVER_HEADER, aResource.getContext().getProxymaVersion());

        //Add Content-Type header
        listPage.addHeader(CONTENT_TYPE_HEADER, "text/html;charset="+charsetEncoding);

        //Add headers for the cache providers to avoid to cache this resource
        listPage.addHeader(CACHE_CONTROL_HEADER, NO_CACHE);
        listPage.addHeader(PRAGMA_HEADER, NO_CACHE);

        //Prepare the byte buffer with the page content.
        ByteBuffer out = ByteBufferFactory.createNewByteBuffer(aResource.getContext());

        //write the header of the page
        byte[] data = testPageData.getBytes(charsetEncoding);
        out.appendBytes(data,data.length);

        //add the buffer to the response
        listPage.setData(out);

        //add the response data to the resource
        aResource.getResponse().setResponseData(listPage);

        log.finer("Test page creation completed.");
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
     * The logger of the context..
     */
    private Logger log = null;

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
     * The name of this plugin.
     */
    private static final String name = "Test Page Retriver";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This retriver plugin simply generates a test page for (obviously) testing puropses.";

    /**
     * The body of the generated test page.
     */
    private static final String testPageData = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
            "<html>\n" +
            "<head><title>Proxyma Test Page</title>\n" +
            "<style type=\"text/css\">p{font-family: Verdana, Arial, Helvetica, sans-serif; font-size: small;}</style></head>\n" +
            "<body><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"medium\"><b>Proxyma TestPage</b><hr/>\n" +
            "<br/><p>This it the test page generated by the \"Test Page Retriver\".</p>\n" +
            "</body>\n</html>\n";
}

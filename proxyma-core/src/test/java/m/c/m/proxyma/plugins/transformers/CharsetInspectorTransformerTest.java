package m.c.m.proxyma.plugins.transformers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ResourceHandler;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * Test the functionality of the CharsetInspectorTransformer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: CharsetInspectorTransformerTest.java 167 2010-06-30 23:40:44Z marcolinuz $
 */
public class CharsetInspectorTransformerTest extends TestCase {
    
    public CharsetInspectorTransformerTest(String testName) {
        super(testName);
    }

   @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Prepare the environment..
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "myServlet/style.css", TestServlet.class.getName() );
        ServletUnitClient sc = sr.newClient();
        WebRequest wreq   = new GetMethodWebRequest( "http://test.meterware.com/myServlet/style.css" );
        wreq.setParameter( "color", "red" );
        WebResponse wres = sc.getResponse( wreq );
        InvocationContext ic = sc.newInvocation( wreq );
        request = ic.getRequest();
        response = ic.getResponse();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        //Cleanup the pool
        try {
            ProxymaFacade proxyma = new ProxymaFacade();
            ProxymaContext context = proxyma.getContextByName("default");
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of process method, of class HtmlUrlRewriteTransformer.
     */
    public void testProcess() throws Exception {
        System.out.println("process");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");


        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/images", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/disney", context);
        ProxymaResponseDataBean responseData = createTestResponse(context);
        aResource.getResponse().setResponseData(responseData);
        aResource.setProxyFolder(folder1);

        ResourceHandler instance = new CharsetInspectorTransformer(context);
        instance.process(aResource);

        ProxymaResponseDataBean data = aResource.getResponse().getResponseData();
        ByteBufferReader thepage = ByteBufferFactory.createNewByteBufferReader(data.getData());
        byte[] result = thepage.getWholeBufferAsByteArray();
        String resultString = new String(result,"ISO-8859-1");

        assertTrue(resultString.startsWith("<html>"));
        assertTrue(responseData.containsHeader(CONTENT_TYPE_HEADER));
        assertEquals("text/html; charset=iso-8859-1", responseData.getHeader(CONTENT_TYPE_HEADER).getValue());
        assertTrue(resultString.endsWith("</html>"));

        proxyma.removeProxyFolder(folder1, context);
        proxyma.removeProxyFolder(folder2, context);
    }


     /**
     * Generates a new response page with the list of the configured folders.<br/>
     * The fields of the list are: folderName, Destination, folderStatus.<br/>
     * Note: this methos is made to be invoked only by the proxy engine.
     * @param contezt the context that contains the registered folders to show.
     * @return a new response data bean ready to be sent to the client
     */
    private ProxymaResponseDataBean createTestResponse(ProxymaContext context) {
        ProxymaResponseDataBean testPage = new ProxymaResponseDataBean();
        String charsetEncoding = context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING);

        //Set response status
        testPage.setStatus(STATUS_OK);

        //Add Date header
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        testPage.addHeader(DATE_HEADER, format.format(new Date()));

        //Add Server header
        testPage.addHeader(SERVER_HEADER, context.getProxymaVersion());

        //Add Content-Type header
        testPage.addHeader(CONTENT_TYPE_HEADER, "text/html");

        //Prepare the byte buffer with the page content.
        try {
            ByteBuffer out = ByteBufferFactory.createNewByteBuffer(context);

            //write the header of the page
            byte[] data = html_test_template.getBytes(charsetEncoding);
            out.appendBytes(data,data.length);

            //add the buffer to the response
            testPage.setData(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return testPage;
    }

    private HttpServletRequest request;
    private HttpServletResponse response;

    private static final int STATUS_OK = 200;
    private static final String DATE_HEADER = "Date";
    private static final String SERVER_HEADER = "Server";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String html_test_template = "" +
        "<html>\n" +
        "<head>\n" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
        "</head>\n" +
        "</html>";

}

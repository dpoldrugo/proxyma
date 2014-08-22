package m.c.m.proxyma.plugins.retrivers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ResourceHandler;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * Test the functionality of the SimpleHttpRetriver
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: SimpleHttpRetriverTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class SimpleHttpRetriverTest extends TestCase {
    
    public SimpleHttpRetriverTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //Prepare the environment..
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "myServlet", TestServlet.class.getName() );
        ServletUnitClient sc = sr.newClient();
        WebRequest wreq   = new GetMethodWebRequest( "http://test.meterware.com/myServlet" );
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
     * Test of process method, of class SimpleHttpRetriver.
     */
    public void testProcess() throws Exception {
        System.out.println("process");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        ProxyFolderBean folder = proxyma.createNewProxyFolder("testFolder", "http://proxyma.sourceforge.net", context);
        aResource.setProxyFolder(folder);
        aResource.setDestinationSubPath("/");

        ResourceHandler instance = new SimpleHttpRetriver(context);

        try {
        instance.process(aResource);
        } catch (IOException e) {
            //internet connection not available?

            //cleanupThe context
            proxyma.removeProxyFolder(folder, context);
            fail("Unable to connect to the remote server for the test.. continue");
        }

        ProxymaResponseDataBean data = aResource.getResponse().getResponseData();

        assertSame(data, aResource.getAttribute("Original-Response"));

        ByteBufferReader thepage = ByteBufferFactory.createNewByteBufferReader(data.getData());
        byte[] result = thepage.getWholeBufferAsByteArray();
        String resultString = new String(result,context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING));

        assertTrue(resultString.startsWith("<?xml version='1.0'?>\n"));
        assertTrue(resultString.contains("<title>Proxyma - HomePage</title>"));
        assertTrue(resultString.endsWith("</html><!-- Content End -->"));

        //cleanupThe context
        proxyma.removeProxyFolder(folder, context);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}

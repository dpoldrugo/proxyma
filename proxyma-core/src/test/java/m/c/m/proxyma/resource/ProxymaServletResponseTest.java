package m.c.m.proxyma.resource;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * Test the functionality of the ProxymaServletResponse
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaServletResponseTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxymaServletResponseTest extends TestCase {
    
    public ProxymaServletResponseTest(String testName) {
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
        WebRequest wreq   = new GetMethodWebRequest( "http://test.meterware.com/myServlet?a=1&b=2" );
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
     * Test of sendDataToClient method, of class ProxymaServletResponse.
     */
    public void testSendDataToClient() throws Exception {
        System.out.println("sendDataToClient");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaResponseDataBean dataBean = new ProxymaResponseDataBean();
        ByteBuffer data = ByteBufferFactory.createNewByteBuffer(context);
        data.appendBytes("Hello World!".getBytes(), "Hello World!".getBytes().length);

        dataBean.addHeader("Content-Type", "text/plain");
        dataBean.addHeader("Age", "1");
        dataBean.addCookie(new Cookie("test", "TestCookie"));
        dataBean.addCookie(new Cookie("Session", "TestSession"));
        dataBean.setStatus(200);

        dataBean.setData(data);

        ProxymaResponseDataBean clone = (ProxymaResponseDataBean)dataBean.clone();
        clone.setData(null);

        ProxymaServletResponse theResponse = new ProxymaServletResponse(response, context);
        theResponse.setResponseData(dataBean);

        int result = theResponse.sendDataToClient();
        assertEquals(200, result);

        try {
            theResponse.sendDataToClient();
            fail("exception not thrown");
        } catch (IllegalStateException x) {
            assertTrue(true);
        }

        ProxymaServletResponse anotherResponse = new ProxymaServletResponse(response, context);
        anotherResponse.setResponseData(clone);
        result = anotherResponse.sendDataToClient();
        assertEquals(result, HttpServletResponse.SC_OK);

    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}

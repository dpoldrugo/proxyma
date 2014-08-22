package m.c.m.proxyma.plugins.serializers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.File;
import java.util.Date;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ResourceHandler;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * Test the functionality of the PerformanceTestSerializer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: PerformanceTestSerializerTest.java 163 2010-06-28 23:03:13Z marcolinuz $
 */
public class PerformanceTestSerializerTest extends TestCase {
    
    public PerformanceTestSerializerTest(String testName) {
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
     * Test of process method, of class SimpleSerializer.
     */
    public void testProcess() throws Exception {
        System.out.println("process");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        Date previous = new Date();

        ProxymaResponseDataBean dataBean = new ProxymaResponseDataBean();
        ByteBuffer data = ByteBufferFactory.createNewByteBuffer(context);
        data.appendBytes("Hello World!".getBytes(), "Hello World!".getBytes().length);

        dataBean.addHeader("Content-Type", "text/plain");
        dataBean.addHeader("Age", "1");
        dataBean.addCookie(new Cookie("test", "TestCookie"));
        dataBean.addCookie(new Cookie("Session", "TestSession"));
        dataBean.setStatus(200);

        dataBean.setData(data);
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        aResource.getResponse().setResponseData(dataBean);
        aResource.addAttibute("Timestamp", previous);
        Thread.sleep(100);

        ResourceHandler serializer = new PerformanceTestSerializer(context);
        serializer.process(aResource);

        assertFalse(dataBean.containsHeader("X-Forwarded-For"));
        assertEquals(dataBean.getHeader("Content-Length").getValue(), Integer.toString((int)dataBean.getData().getSize()));

        String logsDirectory = context.getLogsDirectoryPath() + "proxyma-" + context.getName() + "-performance.log";
        File log = new File(logsDirectory);
        assertTrue(log.exists());
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}

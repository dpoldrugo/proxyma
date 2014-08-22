package m.c.m.proxyma.plugins.preprocessors;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * Test the functionality of the StoreTimestampPreprocessor
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: StoreTimestampPreprocessorTest.java 157 2010-06-27 19:24:02Z marcolinuz $
 */
public class StoreTimestampPreprocessorTest extends TestCase {
    
    public StoreTimestampPreprocessorTest(String testName) {
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
     * Test of process method, of class StoreTimestampPreprocessor.
     */
    public void testProcess() {
        System.out.println("process");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        StoreTimestampPreprocessor instance = new StoreTimestampPreprocessor(context);

        //Create a testpage for the tests..
        instance.process(aResource);

        //Check for the timestamp..
        Object theAttribute = aResource.getAttribute("Timestamp");
        assertNotNull(theAttribute);
        assertTrue(theAttribute instanceof Date);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}

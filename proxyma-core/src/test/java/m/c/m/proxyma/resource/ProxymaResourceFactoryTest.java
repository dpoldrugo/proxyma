package m.c.m.proxyma.resource;

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
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxymaContext;
import org.xml.sax.SAXException;

/**
 * <p>
 * Test the functionality of the ProxymaResourceFactory
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaResourceFactoryTest.java 157 2010-06-27 19:24:02Z marcolinuz $
 */
public class ProxymaResourceFactoryTest extends TestCase {

    /**
     * Test of createNewResource method, of class ResourceFactory.
     */
    public void testCreateNewResourceInstance() throws IOException, SAXException {
        System.out.println("createNewResourceInstance");

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
        HttpServletRequest request = ic.getRequest();
        HttpServletResponse response = ic.getResponse();
        ProxymaResourceFactory factory = new ProxymaResourceFactory();
        ProxymaResource instance = factory.createNewResource(request, response, context);

        //Test resource creation
        assertNotNull(instance);
        assertSame(context, instance.getContext());
        assertTrue(instance.getRequest() instanceof ProxymaServletRequest);
        assertTrue(instance.getResponse() instanceof ProxymaServletResponse);

        //Cleanup the pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}

package m.c.m.proxyma.core;

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
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.resource.ProxymaResource;
import org.xml.sax.SAXException;

/**
 * <p>
 * Test the functionality of the ProxyEngine
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxyEngineTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxyEngineTest extends TestCase {

    public ProxyEngineTest(String testName) {
        super(testName);
    }

    /**
     * Test of doProxy method, of class ProxyEngine.
     */
    public void testDoProxy() throws IOException, SAXException, IllegalAccessException, Exception {
        System.out.println("doProxy");

        //Prepare the environment..
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "myServlet", TestServlet.class.getName() );
        ServletUnitClient sc = sr.newClient();

        System.out.println(" --->test contextroot (expected redirect)");
        WebRequest wreq   = new GetMethodWebRequest( "http://localhost:0/myServlet");
        WebResponse wres = sc.getResponse( wreq );
        InvocationContext ic = sc.newInvocation( wreq );
        HttpServletRequest request = ic.getRequest();
        HttpServletResponse response = ic.getResponse();
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        ProxyEngine instance = proxyma.createNewProxyEngine(context);
        ProxyFolderBean folder = proxyma.createNewProxyFolder("google", "http://www.google.com", context);
        folder = proxyma.createNewProxyFolder("apple", "http://www.apple.com", context);
        folder.setEnabled(false);

        int retval = instance.doProxy(aResource);
        assertEquals(302, retval);
        assertEquals(context.getProxymaVersion(), aResource.getResponse().getResponseData().getHeader("Server").getValue());

       

        //Cleanup the pool
        try {
            proxyma.removeProxyFolder(proxyma.getProxyFolderByURLEncodedName("google", context), context);
            proxyma.removeProxyFolder(proxyma.getProxyFolderByURLEncodedName("apple", context), context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}

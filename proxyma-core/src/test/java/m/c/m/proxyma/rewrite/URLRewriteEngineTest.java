package m.c.m.proxyma.rewrite;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * Test the functionality of the RewriterEngine
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: URLRewriteEngineTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class URLRewriteEngineTest extends TestCase {
    
    public URLRewriteEngineTest(String testName) {
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
     * Test of masqueradeURL method, of class URLRewriteEngine.
     */
    public void testMasqueradeURL() throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        System.out.println("masqueradeURL");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");
        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/it", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/en", context);
        aResource.setProxyFolder(folder1);
        URLRewriteEngine instance = new URLRewriteEngine(context);

        String theUrl = "http://www.yahoo.it/profile/it.html?a=1&b=2";
        String expected = "http://www.yahoo.it/profile/it.html?a=1&b=2";
        String result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);

        theUrl = "http://www.google.com:80/it/profile/io.html?a=1&b=2";
        expected = "/proxyma/host1/profile/io.html?a=1&b=2";
        result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);

        theUrl = "/it/profile/io.html";
        expected = "/proxyma/host1/profile/io.html";
        result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);

        theUrl = "/anotherRoot/it/profile/io.html";
        expected = "http://www.google.com/anotherRoot/it/profile/io.html";
        result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);

        theUrl = "profile/io.html";
        expected = "profile/io.html";
        result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);


        theUrl = "https://www.apple.com:443/en/macbook/new.html?a=1&b=2";
        expected = "/proxyma/host2/macbook/new.html?a=1&b=2";
        result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);

        proxyma.removeProxyFolder(folder2, context);

        theUrl = "https://www.apple.com/en/macbook/new.html";
        expected = "https://www.apple.com/en/macbook/new.html";
        result = instance.masqueradeURL(theUrl, aResource);
        assertEquals(expected, result);


        proxyma.removeProxyFolder(folder1, context);
    }

   
    private HttpServletRequest request;
    private HttpServletResponse response;
}

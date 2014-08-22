package m.c.m.proxyma.rewrite;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;import javax.servlet.http.Cookie;
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
 * @version $Id: CookieRewriteEngineTest.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class CookieRewriteEngineTest extends TestCase {
    
    public CookieRewriteEngineTest(String testName) {
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

    public void testMasquerade_Unmasquerade_Cookie() throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        System.out.println("masquerade/unmasqueradeCookie");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/it", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/en", context);
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");
        aResource.setProxyFolder(folder1);
        CookieRewriteEngine instance = new CookieRewriteEngine(context);

        Cookie theCookie = new Cookie("cookie1", "Value1");
        theCookie.setDomain("google.com");
        theCookie.setPath("/it/pippo");
        instance.masqueradeCookie(theCookie, aResource);

        String expected = "localhost";
        assertEquals(expected, theCookie.getDomain());

        expected = "/proxyma/host1/pippo";
        assertEquals(expected, theCookie.getPath());

        expected = CookieRewriteEngine.PROXYMA_REWRITTEN_HEADER  + "Value1";
        assertEquals(expected, theCookie.getValue());

        instance.unmasqueradeCookie(theCookie);

        expected = "Value1";
        assertEquals(expected, theCookie.getValue());

        theCookie = new Cookie("cookie2", "Value2");
        instance.masqueradeCookie(theCookie, aResource);

        expected = "localhost";
        assertEquals(expected, theCookie.getDomain());

        expected = "/proxyma/host1";
        assertEquals(expected, theCookie.getPath());

        expected = CookieRewriteEngine.PROXYMA_REWRITTEN_HEADER  + "Value2";
        assertEquals(expected, theCookie.getValue());

        instance.unmasqueradeCookie(theCookie);

        expected = "Value2";
        assertEquals(expected, theCookie.getValue());

        proxyma.removeProxyFolder(folder2, context);
        proxyma.removeProxyFolder(folder1, context);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}

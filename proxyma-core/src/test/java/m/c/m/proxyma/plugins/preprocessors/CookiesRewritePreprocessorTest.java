package m.c.m.proxyma.plugins.preprocessors;

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
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.rewrite.CookieRewriteEngine;

/**
 * <p>
 * Test the functionality of the CookiesRewriterPreprocessor
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: CookiesRewritePreprocessorTest.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class CookiesRewritePreprocessorTest extends TestCase {
    
    public CookiesRewritePreprocessorTest(String testName) {
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
     * Test of process method, of class CookiesRewritePreprocessor.
     */
    public void testProcess() {
       System.out.println("process");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResource(new FakeCookieServlet(), response, context);
        CookiesRewritePreprocessor instance = new CookiesRewritePreprocessor(context);

        Cookie[] requestCookies = aResource.getRequest().getCookies();
        assertEquals(1,requestCookies.length);
        Cookie theCookie = requestCookies[0];

        //Create a testpage for the tests..
        instance.process(aResource);
        
        requestCookies = aResource.getRequest().getCookies();
        assertEquals(1,requestCookies.length);
        theCookie = requestCookies[0];
        assertEquals("To protect your privacy, the Proxyma-NG CookiesRewritePreprocessor has ereased this Cookie.", theCookie.getValue());

        theCookie.setValue(CookieRewriteEngine.PROXYMA_REWRITTEN_HEADER + "rewritten-value");

        //Create a testpage for the tests..
        instance.process(aResource);

        requestCookies = aResource.getRequest().getCookies();
        assertEquals(1,requestCookies.length);
        theCookie = requestCookies[0];
        assertEquals("rewritten-value", theCookie.getValue());

    }

    private HttpServletRequest request;
    private HttpServletResponse response;
}

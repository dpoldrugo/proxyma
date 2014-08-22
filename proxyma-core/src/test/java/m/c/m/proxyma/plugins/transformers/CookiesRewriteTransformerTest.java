package m.c.m.proxyma.plugins.transformers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import m.c.m.proxyma.rewrite.CookieRewriteEngine;

/**
 * <p>
 * Test the functionality of the CookiesRewriterTransformer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: CookiesRewriteTransformerTest.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class CookiesRewriteTransformerTest extends TestCase {
    
    public CookiesRewriteTransformerTest(String testName) {
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
        wreq.setHeaderField("Cookie", "rewritten=value1");
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
     * Test of process method, of class CookiesRewriteTransformer.
     */
    public void testProcess() throws Exception {
       System.out.println("process");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");
        ProxymaResource aResource = proxyma.createNewResource(request, response, context);
        CookiesRewriteTransformer instance = new CookiesRewriteTransformer(context);

        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/it", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/en", context);
        ProxymaResponseDataBean responseData = new ProxymaResponseDataBean();
        aResource.getResponse().setResponseData(responseData);
        aResource.setProxyFolder(folder1);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");

        Cookie aCookie = new Cookie("cookie1", "value1");
        aCookie.setDomain("google.com");
        aCookie.setPath("/it/goofy");
        responseData.addCookie(aCookie);

        aCookie = new Cookie("cookie2", "value2");
        responseData.addCookie(aCookie);

        //Create a testpage for the tests..
        instance.process(aResource);

        Collection<Cookie> responseCookies = aResource.getResponse().getResponseData().getCookies();
        assertEquals(2,responseCookies.size());

        Iterator<Cookie> iterator = responseCookies.iterator();
        while (iterator.hasNext()) {
            aCookie = iterator.next();
            if ("cookie1".equals(aCookie.getName())) {
                assertEquals("localhost", aCookie.getDomain());
                assertEquals("/proxyma/host1/goofy", aCookie.getPath());
                assertEquals(CookieRewriteEngine.PROXYMA_REWRITTEN_HEADER+"value1", aCookie.getValue());
            } else {
                assertEquals("localhost", aCookie.getDomain());
                assertEquals("/proxyma/host1", aCookie.getPath());
                assertEquals(CookieRewriteEngine.PROXYMA_REWRITTEN_HEADER+"value2", aCookie.getValue());
            }
        }

        proxyma.removeProxyFolder(folder1, context);
        proxyma.removeProxyFolder(folder2, context);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;

}

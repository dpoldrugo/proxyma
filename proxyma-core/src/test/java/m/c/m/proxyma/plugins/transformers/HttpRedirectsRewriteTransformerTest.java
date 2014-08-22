package m.c.m.proxyma.plugins.transformers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * Test the functionality of the HttpRedirectsRewriterTransformer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: HttpRedirectsRewriteTransformerTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class HttpRedirectsRewriteTransformerTest extends TestCase {
    
    public HttpRedirectsRewriteTransformerTest(String testName) {
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
        HttpRedirectsRewriteTransformer instance = new HttpRedirectsRewriteTransformer(context);

        ProxyFolderBean folder1 = proxyma.createNewProxyFolder("host1", "http://www.google.com/it", context);
        ProxyFolderBean folder2 = proxyma.createNewProxyFolder("host2", "https://www.apple.com/en", context);
        ProxymaResponseDataBean responseData = new ProxymaResponseDataBean();
        aResource.getResponse().setResponseData(responseData);
        aResource.setProxyFolder(folder1);
        aResource.setProxymaRootURI("http://localhost:8080/proxyma");
        responseData.setStatus(302);

        //Call the method to test
        instance.process(aResource);
        assertNull(responseData.getHeader("Location"));

        responseData.addHeader("Location", "https://www.apple.com/en/goofy/newResource.html");
        instance.process(aResource);

        assertEquals("http://localhost:8080/proxyma/host2/goofy/newResource.html", responseData.getHeader("Location").getValue());


        proxyma.removeProxyFolder(folder1, context);
        proxyma.removeProxyFolder(folder2, context);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;

}

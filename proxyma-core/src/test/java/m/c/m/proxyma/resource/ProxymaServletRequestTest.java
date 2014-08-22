package m.c.m.proxyma.resource;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * Test the functionality of the ProxymaServletRequest
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaServletRequestTest.java 176 2010-07-03 09:02:14Z marcolinuz $
 */
public class ProxymaServletRequestTest extends TestCase {
    
    public ProxymaServletRequestTest(String testName) {
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
     * Test of getHeaderNames method, of class ProxymaServletRequest.
     */
    public void testGetHeaderNames() {
        System.out.println("getHeaderNames");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        Enumeration expResult = request.getHeaderNames();
        Enumeration result = instance.getHeaderNames();
        assertEquals(expResult.hasMoreElements(), result.hasMoreElements());

        while (expResult.hasMoreElements()) {
            String servHeadName = (String)expResult.nextElement();
            String headName = (String)result.nextElement();
            assertEquals(servHeadName, headName);
        }
    }

    /**
     * Test of getHeader method, of class ProxymaServletRequest.
     */
    public void testGetHeader() {
        System.out.println("getHeader");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getHeader("Host");
        String result = instance.getHeader("Host");
        assertEquals(expResult, result);
    }

    /**
     * Test of getHeaders method, of class ProxymaServletRequest.
     */
    public void testGetHeaders() {
        System.out.println("getHeaders");
        System.out.println("getHeader");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        Enumeration expResult = request.getHeaders("Accept");
        Enumeration result = instance.getHeaders("Accept");
        while (expResult.hasMoreElements()) {
            String servHeadName = (String)expResult.nextElement();
            String headName = (String)result.nextElement();
            assertEquals(servHeadName, headName);
        }
    }

    /**
     * Test of getContextURLPath method, of class ProxymaServletRequest.
     */
    public void testGetSubPath() {
        System.out.println("getSubPath");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getPathInfo();
        String result = instance.getSubPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getContextURLPath method, of class ProxymaServletRequest.
     */
    public void testGetContextPath() {
        System.out.println("getContextPath");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getContextPath() + request.getServletPath();
        String result = instance.getContextURLPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRequestURI method, of class ProxymaServletRequest.
     */
    public void testGetRequestURI() {
        System.out.println("getRequestURI");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getRequestURI();
        String result = instance.getRequestURI();
        assertEquals(expResult, result);
    }

    /**
     * Test of getQueryString method, of class ProxymaServletRequest.
     */
    public void testGetQueryString() {
        System.out.println("getQueryString");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getQueryString();
        String result = instance.getQueryString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMethod method, of class ProxymaServletRequest.
     */
    public void testGetMethod() {
        System.out.println("getMethod");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getMethod();
        String result = instance.getMethod();
        assertEquals(expResult, result);
    }

        /**
     * Test of getMethod method, of class ProxymaServletRequest.
     */
    public void testParameterNames() {
        System.out.println("getParameterNames");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        Enumeration expResult = request.getParameterNames();
        Enumeration result = instance.getParameterNames();
        assertEquals(expResult.hasMoreElements(), result.hasMoreElements());
        assertEquals(expResult.nextElement(), result.nextElement());
    }

    /**
     * Test of getProtocol method, of class ProxymaServletRequest.
     */
    public void testGetProtocol() {
        System.out.println("getProtocol");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getProtocol();
        String result = instance.getProtocol();
        assertEquals(expResult, result);
    }

    /**
     * Test of getContentType method, of class ProxymaServletRequest.
     */
    public void testGetContentType() {
        System.out.println("getContentType");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getContentType();
        String result = instance.getContentType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCharacterEncoding method, of class ProxymaServletRequest.
     */
    public void testGetCharacterEncoding() {
        System.out.println("getCharacterEncoding");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getCharacterEncoding();
        String result = instance.getCharacterEncoding();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCookies method, of class ProxymaServletRequest.
     */
    public void testGetCookies() {
        System.out.println("getCookies");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        Cookie[] expResult = request.getCookies();
        Cookie[] result = instance.getCookies();
        assertEquals(expResult.length, result.length);

        for (int i=0; i< result.length; i++) {
            assertEquals(expResult[i].getName(), result[i].getName());
        }
    }

    /**
     * Test of getServerName method, of class ProxymaServletRequest.
     */
    public void testGetServerName() {
        System.out.println("getServerName");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getServerName();
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerPort method, of class ProxymaServletRequest.
     */
    public void testGetServerPort() {
        System.out.println("getServerPort");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        int expResult = request.getServerPort();
        int result = instance.getServerPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRemoteHost method, of class ProxymaServletRequest.
     */
    public void testGetRemoteHost() {
        System.out.println("getRemoteHost");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getRemoteHost();
        String result = instance.getRemoteHost();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRemoteAddress method, of class ProxymaServletRequest.
     */
    public void testGetRemoteAddress() {
        System.out.println("getRemoteAddress");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getRemoteAddr();
        String result = instance.getRemoteAddress();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRemoteUser method, of class ProxymaServletRequest.
     */
    public void testGetRemoteUser() {
        System.out.println("getRemoteUser");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.getContextByName("default");

        ProxymaServletRequest instance = new ProxymaServletRequest(request, context);
        String expResult = request.getRemoteUser();
        String result = instance.getRemoteUser();
        assertEquals(expResult, result);
    }

    private HttpServletRequest request;
    private HttpServletResponse response;

}

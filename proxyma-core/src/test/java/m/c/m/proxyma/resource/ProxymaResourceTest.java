package m.c.m.proxyma.resource;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.TestServlet;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import org.apache.commons.lang.NullArgumentException;
import org.xml.sax.SAXException;

/**
 * <p>
 * Test the functionality of the ProxymaResource
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaResourceTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxymaResourceTest extends TestCase {
    
    public ProxymaResourceTest(String testName) {
        super(testName);
    }

    /**
     * Test of get Context/Request/Response method, of class ProxymaResource.
     */
    public void testGetContextRequestResponse() throws IOException, SAXException {
        System.out.println("getContext");

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
        ProxymaServletRequest proxumaReq = new ProxymaServletRequest(request, context);
        ProxymaServletResponse proxymaRes = new ProxymaServletResponse(response, context);
        ProxymaResource instance = new ProxymaResource(proxumaReq, proxymaRes, context);
        assertSame(context, instance.getContext());

        assertSame(proxumaReq, instance.getRequest());

        assertSame(proxymaRes, instance.getResponse());

        //Cleanup the pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of get/set ProxyFolder method, of class ProxymaResource.
     */
    public void testGetSetProxyFolder() throws IOException, SAXException {
        System.out.println("get/set ProxyFolder");

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
        ProxymaServletRequest proxumaReq = new ProxymaServletRequest(request, context);
        ProxymaServletResponse proxymaRes = new ProxymaServletResponse(response, context);
        ProxymaResource instance = new ProxymaResource(proxumaReq, proxymaRes, context);
        //run the specific tests
        ProxyFolderBean folder = proxyma.createNewProxyFolder("default", "http://www.google.com", context);
        ProxyFolderBean expResult = folder;

        //Test null proxyfolder
        try {
            instance.setProxyFolder(null);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        //Set a proxyFolder
        instance.setProxyFolder(folder);
        assertSame(expResult, instance.getProxyFolder());

        //Cleanup the pool
        try {
            proxyma.removeProxyFolder(folder, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getAttribute method, of class ProxymaResource.
     */
    public void testGetAttribute() throws IOException, SAXException {
        System.out.println("add/get Attribute");

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
        ProxymaServletRequest proxumaReq = new ProxymaServletRequest(request, context);
        ProxymaServletResponse proxymaRes = new ProxymaServletResponse(response, context);
        ProxymaResource instance = new ProxymaResource(proxumaReq, proxymaRes, context);
        //run the specific tests
        String anAttributeName = null;
        Object expResult = null;

        //null attribute name
        try {
            anAttributeName = null;
            expResult = "testValue";
            instance.addAttibute(anAttributeName, expResult);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        //null attribute value
        try {
            anAttributeName = "testAttribute";
            expResult = null;
            instance.addAttibute(anAttributeName, expResult);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        //regular attribute
        anAttributeName = "testAttribute";
        expResult = "oldValue";
        instance.addAttibute(anAttributeName, expResult);
        Object result = instance.getAttribute(anAttributeName);
        assertSame(expResult, result);

        //Attribute overwrite
        anAttributeName = "testAttribute";
        expResult = "newValue";
        instance.addAttibute(anAttributeName, expResult);
        result = instance.getAttribute(anAttributeName);
        assertSame(expResult, result);

        //Cleanup the pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of deleteAttribute method, of class ProxymaResource.
     */
    public void testDeleteAttribute() throws IOException, SAXException {
        System.out.println("deleteAttribute");

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
        ProxymaServletRequest proxumaReq = new ProxymaServletRequest(request, context);
        ProxymaServletResponse proxymaRes = new ProxymaServletResponse(response, context);
        ProxymaResource instance = new ProxymaResource(proxumaReq, proxymaRes, context);
        //run the specific tests
        String anAttributeName = null;
        Object expResult = null;

        //add regular attribute
        anAttributeName = "testAttribute";
        expResult = "attrValue";
        instance.addAttibute(anAttributeName, expResult);
        Object result = instance.getAttribute(anAttributeName);
        assertSame(expResult, result);

        //null attribute name
        try {
            anAttributeName = null;
            instance.deleteAttribute(anAttributeName);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        //unexisting attribute name (nothing has to be done)
        anAttributeName = "unexistingAttribute";
        instance.deleteAttribute(anAttributeName);
        assertEquals(1, instance.getAttributeNames().size());


        //Attribute remove
        anAttributeName = "testAttribute";
        expResult = "newValue";
        instance.deleteAttribute(anAttributeName);
        result = instance.getAttribute(anAttributeName);
        assertNull(result);
        assertEquals(0, instance.getAttributeNames().size());

        //Cleanup the pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getAttributeNames method, of class ProxymaResource.
     */
    public void testGetAttributeNames() throws IOException, SAXException {
        System.out.println("getAttributeNames");

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
        ProxymaServletRequest proxumaReq = new ProxymaServletRequest(request, context);
        ProxymaServletResponse proxymaRes = new ProxymaServletResponse(response, context);
        ProxymaResource instance = new ProxymaResource(proxumaReq, proxymaRes, context);

        //run the specific tests
        String anAttributeName = null;
        Object expResult = null;
        Object result = null;
        Collection<String> col = null;

        //regular attributes
        anAttributeName = "testAttribute1";
        expResult = "attrValue1";
        instance.addAttibute(anAttributeName, expResult);
        result = instance.getAttribute(anAttributeName);
        assertSame(expResult, result);

        anAttributeName = "testAttribute2";
        expResult = "attrValue2";
        instance.addAttibute(anAttributeName, expResult);
        result = instance.getAttribute(anAttributeName);
        assertSame(expResult, result);

        col = instance.getAttributeNames();
        assertEquals(2, col.size());

        Iterator<String> iter = col.iterator();
        String string = iter.next();
        if (string.equals("testAttribute1")) {
            string = iter.next();
            assertEquals("testAttribute2", string);
        } else {
            string = iter.next();
            assertEquals("testAttribute1", string);
        }
        

        //Cleanup the pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}

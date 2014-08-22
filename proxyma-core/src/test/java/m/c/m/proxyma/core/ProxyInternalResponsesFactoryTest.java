package m.c.m.proxyma.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * Test the functionality of the ProxyInternalResponseFactory
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxyInternalResponsesFactoryTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxyInternalResponsesFactoryTest extends TestCase {
    
    public ProxyInternalResponsesFactoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of createRedirectResponse method, of class ProxyInternalResponsesFactory.
     */
    public void testCreateRedirectResponse() {
        System.out.println("createRedirectResponse");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxymaResponseDataBean instance = null;

        String destination = "htpo:/inv,alid.url/";
        try {
            instance = ProxyInternalResponsesFactory.createRedirectResponse(destination, context);
            fail("expected exception not thrown");
        } catch (MalformedURLException ex) {
            assertTrue(true);
        }

        destination = "http://www.google.com/";
        try {
            instance = ProxyInternalResponsesFactory.createRedirectResponse(destination, context);
        } catch (MalformedURLException ex) {
            fail("unexpected malformed url exception thrown");
        }

        assertNotNull(instance.getHeader("date"));
        assertEquals(instance.getHeader("Server").getValue(), context.getProxymaVersion());
        assertEquals(instance.getHeader("Location").getValue(), destination);
        assertEquals(instance.getStatus(), 302);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of createErrorResponse method, of class ProxyInternalResponsesFactory.
     */
    public void testCreateErrorResponse() {
        System.out.println("createErrorResponse");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxymaResponseDataBean instance = null;

        int code = 500;
        instance = ProxyInternalResponsesFactory.createErrorResponse(code, context);

        assertNotNull(instance.getHeader("date"));
        assertEquals(instance.getHeader("Server").getValue(), context.getProxymaVersion());
        assertEquals(instance.getStatus(), code);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }


    /**
     * Test of createFoldersListResponse method, of class ProxyInternalResponsesFactory.
     */
    public void testcreateFoldersListResponse() throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        System.out.println("createFoldersListResponse");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean folder =  proxyma.createNewProxyFolder("GoogleFolder", "http://www.google.com", context);
        folder = proxyma.createNewProxyFolder("AppleFolder", "http://www.apple.com", context);
        ProxymaResponseDataBean instance = null;

        instance = ProxyInternalResponsesFactory.createFoldersListResponse(context);

        assertNotNull(instance.getHeader("date"));
        assertEquals(instance.getHeader("Server").getValue(), context.getProxymaVersion());
        assertEquals(instance.getStatus(), 200);
        
        assertEquals(instance.getHeader("Content-type").getValue(), "text/html;charset="+context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING));
        assertTrue(instance.getContentLenght() > 0);

        ByteBufferReader data = ByteBufferFactory.createNewByteBufferReader(instance.getData());

        byte[] result = data.getWholeBufferAsByteArray();
        String resultString = new String(result,context.getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING));

        assertTrue(resultString.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""));
        assertTrue(resultString.contains("<td align=\"left\"><a href=\"./GoogleFolder/\">GoogleFolder</a></td>"));
        assertTrue(resultString.contains("<td align=\"left\"><a href=\"./AppleFolder/\">AppleFolder</a></td>"));
        assertTrue(resultString.endsWith("</html>\n"));

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(context.getProxyFolderByURLEncodedName("GoogleFolder"), context);
            proxyma.removeProxyFolder(context.getProxyFolderByURLEncodedName("AppleFolder"), context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}

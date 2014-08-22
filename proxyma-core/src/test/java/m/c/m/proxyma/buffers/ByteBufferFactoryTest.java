/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.buffers;

import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * Test the functionality of the ByteBufferFactory
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ByteBufferFactoryTest.java 157 2010-06-27 19:24:02Z marcolinuz $
 */
public class ByteBufferFactoryTest extends TestCase {
    
    /**
     * Test of createNewByteBuffer method, of class ByteBufferFactory.
     */
    public void testCreateNewByteBuffer() throws Exception {
        System.out.println("createNewByteBuffer");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");

        ByteBuffer instance = ByteBufferFactory.createNewByteBuffer(context);
        assertNotNull(instance);
        assertTrue(instance instanceof SmartBuffer);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of createNewByteBufferReader method, of class ByteBufferFactory.
     */
    public void testCreateNewByteBufferReader() throws Exception {
        System.out.println("createNewByteBufferReader");
        System.out.println("createNewByteBuffer");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");

        try {
            ByteBufferReader instance = ByteBufferFactory.createNewByteBufferReader(null);
            fail("Exception not thrown");
        } catch (Exception e) {
            assertTrue(true);
        }

        ByteBuffer abuffer = ByteBufferFactory.createNewByteBuffer(context);
        ByteBufferReader instance = ByteBufferFactory.createNewByteBufferReader(abuffer);
        assertNotNull(instance);
        assertTrue(instance instanceof SmartBufferReader);

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}

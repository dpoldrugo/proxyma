/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.buffers;

import junit.framework.TestCase;

/**
 * <p>
 * Test the functionality of the RamBuffer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: RamBufferTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class RamBufferTest extends TestCase {
    
    public RamBufferTest(String testName) {
        super(testName);
        instance = new RamBuffer(RamBuffer.TEXT_DATA);
    }

    /**
     * Test of appendBytes method, of class RamBuffer.
     */
    public void testAppendBytes() throws Exception {
        System.out.println("appendBytes");
        byte[] data = "Some Data to write into this buffer".getBytes();
        size = data.length;
        long result = instance.appendBytes(data, size);
        assertEquals(size, result);
    }

    /**
     * Test of appendByte method, of class RamBuffer.
     */
    public void testAppendByte() throws Exception {
        System.out.println("appendByte");
        byte[] data = "Some Data to write into this buffer".getBytes();
        size+= data.length;
        long result;

        for (result=0; result<data.length; result++)
            instance.appendByte(data[(int)result]);
        assertEquals(size, result);
    }

    /**
     * Test of lock method, of class RamBuffer.
     */
    public void testLock() {
        System.out.println("lock / isLocked");
        assertFalse(instance.isLocked());
        instance.lock();
        assertTrue(instance.isLocked());
    }

    /**
     * Test of getSize method, of class RamBuffer.
     */
    public void testGetSize() {
        System.out.println("getSize");
        long expResult = size;
        long result = instance.getSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getpageSize method, of class RamBuffer.
     */
    public void testGetpageSize() {
        System.out.println("getpageSize");
        int expResult = RamBuffer.TEXT_DATA;
        int result = instance.getpageSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of clone method, of class RamBuffer.
     */
    public void testClone() throws CloneNotSupportedException {
        System.out.println("clone");

        RamBuffer clone = (RamBuffer) instance.clone();

        assertNotSame(instance, clone);
        assertSame(instance.getPage(0), clone.getPage(0));
        assertEquals(instance.getSize(), clone.getSize());
        assertFalse(instance.isLocked());
        assertTrue(clone.isLocked());
    }

    RamBuffer instance = null;
    int size = 0;
}

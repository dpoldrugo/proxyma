/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.buffers;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * <p>
 * Test the functionality of the SmartBuffer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: SmartBufferTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class SmartBufferTest extends TestCase {
    
    public SmartBufferTest(String testName) {
        super(testName);

        instance = new SmartBuffer(ramsize);
    }

    /**
     * Test of appendBytes method, of class SmartBuffer.
     */
    public void testAppendBytes() throws Exception {
        System.out.println("appendBytes");
        byte[] data = "Some Data to write into this buffer".getBytes();
        size = data.length;
        long result = instance.appendBytes(data, size);
        assertEquals(size, result);

        File tmpfile = new File(instance.getFileBuffer().getFileFullPath());
        assertTrue(tmpfile.isFile());
        assertTrue(tmpfile.canRead());
        assertEquals(size-ramsize, tmpfile.length());
    }

    /**
     * Test of appendByte method, of class SmartBuffer.
     */
    public void testAppendByte() throws Exception {
        System.out.println("appendByte");
        byte[] data = "Some Data to write into this buffer".getBytes();
        size+= data.length;
        long result;
        for (result=0; result<data.length; result++)
            instance.appendByte(data[(int)result]);
        assertEquals(size, result);

        File tmpfile = new File(instance.getFileBuffer().getFileFullPath());
        assertTrue(tmpfile.isFile());
        assertTrue(tmpfile.canRead());
        assertEquals(size-ramsize, tmpfile.length());
    }

    /**
     * Test of lock method, of class SmartBuffer.
     */
    public void testLock() {
        System.out.println("lock / isLocked");
        assertFalse(instance.isLocked());
        instance.lock();
        assertTrue(instance.isLocked());
    }

    /**
     * Test of getSize method, of class SmartBuffer.
     */
    public void testGetSize() {
        System.out.println("getSize");
        long expResult = size;
        long result = instance.getSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of clone method, of class SmartBuffer.
     */
    public void testClone() throws CloneNotSupportedException, IOException {
        System.out.println("clone");

        byte[] data = "Some Data to write into this buffer".getBytes();
        size = data.length;
        long result = instance.appendBytes(data, size);

        SmartBuffer clone = (SmartBuffer) instance.clone();
        assertNotSame(instance, clone);
        assertNotSame(instance.getFileBuffer(), clone.getFileBuffer());
        assertNotSame(instance.getRamBuffer(), clone.getRamBuffer());
        assertEquals(instance.getSize(), clone.getSize());
        assertFalse(instance.isLocked());
        assertTrue(clone.isLocked());
    }

    SmartBuffer instance = null;
    int size = 0;
    int ramsize = 18;
}

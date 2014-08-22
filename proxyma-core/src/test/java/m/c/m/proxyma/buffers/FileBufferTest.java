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
 * Test the functionality of the FileBuffer
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: FileBufferTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class FileBufferTest extends TestCase {
    
    public FileBufferTest(String testName) {
        super(testName);
        try {
            instance = new FileBuffer();
        } catch (IOException x) {
            fail(x.getMessage());
        }
    }

    /**
     * Test of appendBytes method, of class FileBuffer.
     */
    public void testAppendBytes() throws Exception {
        System.out.println("appendBytes");
        byte[] data = "Some Data to write into this buffer".getBytes();
        size = data.length;
        long result = instance.appendBytes(data, size);
        assertEquals(size, result);

        File tmpfile = new File(instance.getFileFullPath());
        assertTrue(tmpfile.isFile());
        assertTrue(tmpfile.canRead());
        assertEquals(tmpfile.length(), size);
    }

    /**
     * Test of appendByte method, of class FileBuffer.
     */
    public void testAppendByte() throws Exception {
        System.out.println("appendByte");
        byte[] data = "Some Data to write into this buffer".getBytes();
        size+= data.length;
        long result;       
        for (result=0; result<data.length; result++)
            instance.appendByte(data[(int)result]);
        assertEquals(size, result);

        File tmpfile = new File(instance.getFileFullPath());
        assertTrue(tmpfile.isFile());
        assertTrue(tmpfile.canRead());
        assertEquals(tmpfile.length(), size);
    }

    /**
     * Test of lock method, of class FileBuffer.
     */
    public void testLock() {
        System.out.println("lock / isLocked");
        assertFalse(instance.isLocked());
        instance.lock();
        assertTrue(instance.isLocked());
    }

    /**
     * Test of getSize method, of class FileBuffer.
     */
    public void testGetSize() {
        System.out.println("getSize");
        long expResult = size;
        long result = instance.getSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileFullPath method, of class FileBuffer.
     */
    public void testGetFileFullPath() {
        System.out.println("getFileFullPath");
        String result = instance.getFileFullPath();
        assertTrue(result.length()>0);
    }

    /**
     * Test of clone method, of class FileBuffer.
     */
    public void testClone() throws CloneNotSupportedException {
        System.out.println("clone");
        String result = instance.getFileFullPath();

        FileBuffer clone = (FileBuffer) instance.clone();

        assertNotSame(instance, clone);
        assertSame(instance.getFileFullPath(), clone.getFileFullPath());
        assertEquals(instance.getSize(), clone.getSize());
        assertFalse(instance.isLocked());
        assertTrue(clone.isLocked());
    }


    FileBuffer instance = null;
    int size = 0;
}

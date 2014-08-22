/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.buffers;

import java.io.IOException;
import junit.framework.TestCase;

/**
 * <p>
 * Test the functionality of the FileBufferReader
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: FileBufferReaderTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class FileBufferReaderTest extends TestCase {
    
    public FileBufferReaderTest(String testName) {
        super(testName);

        try {
            buffer = new FileBuffer();
            originalData = "Some Data to write into this buffer".getBytes();
            size = originalData.length;
            buffer.appendBytes(originalData, size);
        } catch (IOException x) {
            fail(x.getMessage());
        }
    }

    /**
     * Test of readBytes method, of class FileBufferReader.
     */
    public void testReadBytes() throws Exception {
        System.out.println("readBytes");
        FileBufferReader instance = new FileBufferReader(buffer);
        int expResult = (int)buffer.getSize();
        byte[] data = new byte[size+1];

        int result = instance.readBytes(data, size);
        assertEquals(expResult, result);
        
        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);
    }

    /**
     * Test of readByte method, of class FileBufferReader.
     */
    public void testReadByte() throws Exception {
        System.out.println("readByte");
        FileBufferReader instance = new FileBufferReader(buffer);
        int expResult = (int)originalData[0];

        int result = instance.readByte();
        assertEquals(expResult, result);
    }

    /**
     * Test of reset method, of class FileBufferReader.
     */
    public void testReset() throws Exception {
        System.out.println("reset");
        FileBufferReader instance = new FileBufferReader(buffer);
        int expResult = (int)buffer.getSize();
        byte[] data = new byte[size+1];

        int result = instance.readBytes(data, size);
        assertEquals(expResult, result);

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);

        instance.reset();

        result = instance.readBytes(data, size);
        assertEquals(expResult, result);

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);
    }

    /**
     * Test of getWholeBufferAsByteArray method, of class FileBufferReader.
     */
    public void testGetWholeBufferAsByteArray() throws Exception {
        System.out.println("getWholeBufferAsByteArray");
        FileBufferReader instance = new FileBufferReader(buffer);
        int result = (int)buffer.getSize();
        byte[] data = instance.getWholeBufferAsByteArray();

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);
    }

    /**
     * Test of getSize method, of class FileBufferReader.
     */
    public void testGetSize() throws Exception {
        System.out.println("getSize");
        FileBufferReader instance = new FileBufferReader(buffer);
        int expResult = (int)instance.getSize();

        assertEquals(size, expResult);
    }

    private FileBuffer buffer = null;
    byte [] originalData = null;
    private int size = 0;
}

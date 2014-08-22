/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.buffers;

import java.io.IOException;
import javax.naming.spi.DirStateFactory.Result;
import junit.framework.TestCase;

/**
 * <p>
 * Test the functionality of the RamBufferReader
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: RamBufferReaderTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class RamBufferReaderTest extends TestCase {
    
    public RamBufferReaderTest(String testName) {
        super(testName);

         try {
            buffer = new RamBuffer(RamBuffer.TEXT_DATA);
            originalData = "Some Data to write into this buffer".getBytes();
            size = originalData.length;
            buffer.appendBytes(originalData, size);
        } catch (IOException x) {
            fail(x.getMessage());
        }
    }

    /**
     * Test of readBytes method, of class RamBufferReader.
     */
    public void testReadBytes() throws Exception {
        System.out.println("readBytes");
        RamBufferReader instance = new RamBufferReader(buffer);
        int expResult = (int)buffer.getSize();
        byte[] data = new byte[size+1];

        int result = instance.readBytes(data, size);
        assertEquals(expResult, result);

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);
    }

    /**
     * Test of readByte method, of class RamBufferReader.
     */
    public void testReadByte() throws Exception {
        System.out.println("readByte");
        RamBufferReader instance = new RamBufferReader(buffer);
        int expResult = (int)originalData[0];

        int result = instance.readByte();
        assertEquals(expResult, result);
    }

    /**
     * Test of reset method, of class RamBufferReader.
     */
    public void testReset() throws Exception {
        System.out.println("reset");
        RamBufferReader instance = new RamBufferReader(buffer);
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
     * Test of getWholeBufferAsByteArray method, of class RamBufferReader.
     */
    public void testGetWholeBufferAsByteArray() throws Exception {
        System.out.println("getWholeBufferAsByteArray");
        RamBufferReader instance = new RamBufferReader(buffer);
        int result = (int)buffer.getSize();
        byte[] data = instance.getWholeBufferAsByteArray();

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);
    }

    /**
     * Test of getSize method, of class RamBufferReader.
     */
    public void testGetSize() {
        System.out.println("getSize");
        RamBufferReader instance = new RamBufferReader(buffer);
        int expResult = (int)instance.getSize();

        assertEquals(size, expResult);
    }

    /**
     * Test of getBytesLeftCount method, of class RamBufferReader.
     */
    public void testGetBytesLeftCount() throws Exception {
        System.out.println("getBytesLeftCount");
        RamBufferReader instance = new RamBufferReader(buffer);
        int partialRead = 18;
        int expResult = (int)buffer.getSize() - partialRead;
        byte[] data = new byte[size+1];

        instance.readBytes(data, partialRead);
        int result = (int)instance.getBytesLeftCount();
        assertEquals(expResult, result);
    }

    private RamBuffer buffer = null;
    byte [] originalData = null;
    private int size = 0;
}

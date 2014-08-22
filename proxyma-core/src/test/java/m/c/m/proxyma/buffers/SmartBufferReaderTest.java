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
 * Test the functionality of the SmartBufferReader
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: SmartBufferReaderTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class SmartBufferReaderTest extends TestCase {
    
    public SmartBufferReaderTest(String testName) {
        super(testName);

        try {
            buffer = new SmartBuffer(ramsize);
            originalData = "Some Data to write into this buffer".getBytes();
            size = originalData.length;
            buffer.appendBytes(originalData, size);
        } catch (IOException x) {
            fail(x.getMessage());
        }
    }

    /**
     * Test of readBytes method, of class SmartBufferReader.
     */
    public void testReadBytes() throws Exception {
        System.out.println("readBytes");
        SmartBufferReader instance = new SmartBufferReader(buffer);
        int expResult = ramsize;
        byte[] data = new byte[size+1];

        //Reading data only from the ram buffer
        int result = instance.readBytes(data, ramsize);
        assertEquals(expResult, result);

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);

        File tmpfile = new File(buffer.getFileBuffer().getFileFullPath());
        assertTrue(tmpfile.isFile());
        assertTrue(tmpfile.canRead());
        assertEquals(tmpfile.length(), size-ramsize);


        //reading data that overflows the rambuffer (from file)
        expResult = size - ramsize;
        result = instance.readBytes(data, size - ramsize);
        assertEquals(expResult, result);

        for (int i=0; i<result; i++)
            assertEquals(originalData[ramsize+i], data[i]);
    }

    /**
     * Test of readByte method, of class SmartBufferReader.
     */
    public void testReadByte() throws Exception {
        System.out.println("readByte");
        SmartBufferReader instance = new SmartBufferReader(buffer);
        int expResult = (int)originalData[0];

        int result = instance.readByte();
        assertEquals(expResult, result);
    }

    /**
     * Test of reset method, of class SmartBufferReader.
     */
    public void testReset() throws Exception {
        System.out.println("reset");
        SmartBufferReader instance = new SmartBufferReader(buffer);
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
     * Test of getWholeBufferAsByteArray method, of class SmartBufferReader.
     */
    public void testGetWholeBufferAsByteArray() throws Exception {
        System.out.println("getWholeBufferAsByteArray");
        SmartBufferReader instance = new SmartBufferReader(buffer);
        int result = (int)buffer.getSize();
        byte[] data = instance.getWholeBufferAsByteArray();

        for (int i=0; i<result; i++)
            assertEquals(originalData[i], data[i]);
    }

    /**
     * Test of getSize method, of class SmartBufferReader.
     */
    public void testGetSize() throws Exception {
        System.out.println("getSize");
        SmartBufferReader instance = new SmartBufferReader(buffer);
        int expResult = (int)instance.getSize();

        assertEquals(size, expResult);
    }

    private SmartBuffer buffer = null;
    private byte [] originalData = null;
    private int ramsize = 18;
    private int size = 0;
}

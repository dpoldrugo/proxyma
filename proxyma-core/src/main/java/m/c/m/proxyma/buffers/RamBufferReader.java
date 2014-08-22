package m.c.m.proxyma.buffers;

import java.io.IOException;

/**
 * <p>
 * This class implements a Reader Class for the RamBuffer.
 * To read more than once the same data you have to execute reset() method
 * between every read operation.
 * <p></p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * @version $Id: RamBufferReader.java 138 2010-06-20 13:53:32Z marcolinuz $
 */

/**
 * This class is a custom buffer to read and write byte data into RAM
 */
public class RamBufferReader implements ByteBufferReader {

    /**
     * This constructor builds a new Reader based upon the passed RamBuffer.
     *
     * @param buffer the constant value that specifies the type of data to store.
     */
    public RamBufferReader(RamBuffer buffer) {
        pageSize = buffer.getpageSize();
        currOutputpage = buffer.getPage(0);
        size = buffer.getSize();
        theBuffer = buffer;
    }

    /**
     * Reads data from the buffer and stores them into the provided byte array.
     *
     * @param data the byte array where data will be written
     * @param size the max number of data that can be written.
     * @return the number of transfered bytes or -1 if there are no more data to read (the end of the buffer was reached).
     * @throws java.io.IOException
     */
    @Override
    public int readBytes(byte[] data, int size) throws IOException {
        int count = 0;
        if (size > data.length) {
            throw new IOException("Size of data can't be greater than the array's size.");
        } else {
            while ((count < size) && (readPosition < this.size)) {
                for (; (outputPtr < pageSize) && (count < size) && (readPosition < this.size); outputPtr++, count++, readPosition++) {
                    data[count] = currOutputpage[outputPtr];
                }

                if ((outputPtr == pageSize) && (readPosition < this.size)) {
                    //I have to chenge page
                    currOutputpage = nextpage();
                }
            }
        }
        if (count == 0)
            count = -1;
        return count;
    }

    /**
     * Read a single byte of data from the buffer
     *
     * @return the int value of the byte or -1 if the end of the data was reached.
     * @throws java.io.IOException
     */
    @Override
    public int readByte() throws IOException {
        int retVal;
        if (readPosition < this.size) {
            if (outputPtr == pageSize)
                nextpage();
            retVal = currOutputpage[outputPtr];
            outputPtr++;
            readPosition++;
        } else {
            retVal = -1;
        }
        return retVal;
    }

    /**
     * Resets the Reader, next read operation will start fom the begin of the buffer.
     */
    @Override
    public void reset() throws IOException {
        outputPtr = 0;
        currOutputpageNumber = 0;
        readPosition = 0;
        currOutputpage = theBuffer.getPage(currOutputpageNumber);
    }

    /**
     * Returns the whole buffer into a byte array.
     * WARNING! This method could be memory hungry if used with large size buffers.
     *
     * @return the buffer content.
     */
    @Override
    public byte[] getWholeBufferAsByteArray() throws IOException {
        byte[] retVal = new byte[(int) size];

        try {
            readBytes(retVal, (int) size);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retVal;
    }

    /**
     * Returns the size (in bytes) of the data into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    @Override
    public long getSize() {
        return size;
    }

    /**
     * Obtain the number of unreaded bytes into the buffer
     *
     * @return number of unreaded bytes.
     */
    public long getBytesLeftCount() {
        return (size - readPosition);
    }

    /**
     * Move the read pointer to the next page.
     *
     * @return the next page
     */
    private byte[] nextpage() {
        outputPtr = 0;
        return (byte[]) theBuffer.getPage(++currOutputpageNumber);
    }

    //The main container of the data
    private RamBuffer theBuffer;

    //The size of the data into the buffer
    private long size = 0;

    //the absolute position of the read pointer into the buffer
    private long readPosition = 0;

    //The size of a single page
    private int pageSize;

    //The pointers to the current pages
    private byte[] currOutputpage;

    //the pointers to the current page number and posiztion
    private int currOutputpageNumber = 0;
    private int outputPtr = 0;
}

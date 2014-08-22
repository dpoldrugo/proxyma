package m.c.m.proxyma.buffers;

import java.io.IOException;

/**
 * <p>
 * This class implements a Reader Class for the SmartBuffer.
 * To read more than once the same data you have to execute reset() method
 * between every read operation.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p><p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * @version $Id: SmartBufferReader.java 138 2010-06-20 13:53:32Z marcolinuz $
 * </p>
 */
public class SmartBufferReader implements ByteBufferReader {
    /**
     * This constructor builds a new Reader based upon the passed SmartBuffer.
     *
     * @param buffer the SmartBuffer where read data
     */
    public SmartBufferReader(SmartBuffer buffer) throws IOException {
        theBuffer = buffer;
        ramBuf = new RamBufferReader(theBuffer.getRamBuffer());
        if (theBuffer.getFileBuffer() != null) {
            fileBuf = new FileBufferReader(theBuffer.getFileBuffer());
            ramLimitExcedded = true;
        } else {
            ramLimitExcedded = false;
        }
    }

    /**
     * Reads data from the buffer and stores them into the provided byte array.
     *
     * @param data the byte array where data will be written
     * @param size the max number of data that can be written.
     * @return the number of transfered bytes or -1 if there are no more data to read (the end of the buffer was reached).
     * @throws java.io.IOException if something goes wrong
     */
    @Override
    public int readBytes(byte[] data, int size) throws IOException {
        int readedBytes = 0;
        if (ramLimitExcedded) {
            if (readFromFile) {
                readedBytes = fileBuf.readBytes(data, size);
            } else {
                readedBytes = ramBuf.readBytes(data, size);
                if (readedBytes == -1) {
                    readedBytes = fileBuf.readBytes(data, size);
                    readFromFile = true;
                } else if (readedBytes < size) {
                    int toRead = size - readedBytes;
                    for (int i=0; i<toRead; i++)
                        data[readedBytes+i] = (byte)fileBuf.readByte();
                    readedBytes+=toRead;
                    readFromFile = true;
                }
            }
        } else {
            readedBytes = ramBuf.readBytes(data, size);
        }
        return readedBytes;
    }


    /**
     * Read a single byte of data from the buffer
     *
     * @return the int value of the byte or -1 if the end of the data was reached.
     * @throws java.io.IOException if something goes wrong
     */
    @Override
    public int readByte() throws IOException {
        int readedBytes = 0;
        if (ramLimitExcedded) {
            if (readFromFile) {
                readedBytes = fileBuf.readByte();
            } else {
                readedBytes = ramBuf.readByte();
                if (readedBytes == -1) {
                    readFromFile = true;
                    readedBytes = fileBuf.readByte();
                }
            }
        } else {
            readedBytes = ramBuf.readByte();
        }
        return readedBytes;
    }

    /**
     * Resets the Reader, next read operation will start fom the begin of the buffer.
     */
    @Override
    public void reset() throws IOException {
        ramBuf.reset();
        if (ramLimitExcedded)
            fileBuf.reset();
        readFromFile = false;
    }

    /**
     * Returns the whole buffer into a byte array.
     * WARNING! This method could be memory hungry if used with large size buffers.
     *
     * @return the buffer content.
     */
    @Override
    public byte[] getWholeBufferAsByteArray() throws IOException {
        long size = theBuffer.getSize();

        //Load the data into the new buffer.
        byte[] retVal = new byte[(int) (size)];
        readBytes(retVal, (int) size);
        return retVal;
    }

    /**
     * Returns the size (in bytes) of the data into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    @Override
    public long getSize() {
        return theBuffer.getSize();
    }

    //The SmartBuffer
    SmartBuffer theBuffer = null;

    //The internal RamBuffer to store a small amount of data
    private RamBufferReader ramBuf = null;

    //The internal FileBuffer to store large amount of data
    private FileBufferReader fileBuf = null;

    //Flag to set if the data to read are into the fileBuffer.
    private boolean readFromFile = false;

    //Flag to set if the limit was break when writing on the buffer.
    private boolean ramLimitExcedded = false;
}

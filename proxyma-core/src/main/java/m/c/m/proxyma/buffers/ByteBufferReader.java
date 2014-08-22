package m.c.m.proxyma.buffers;

import java.io.IOException;

/**
 * <p>
 * This is a common interface to read into my Buffer classes.
 * It is a Reader that allows many threads to read data at the same time (thread-safe).
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ByteBufferReader.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public interface ByteBufferReader {

    /**
     * Reads the requested amount of data from the buffer and stores
     * them into the provided byte array.
     *
     * @param data the byte array where data will be written
     * @param size the max number of data that can be written.
     * @return the number of transfered bytes or -1 if there are no more data to read (the end of the buffer was reached).
     * @throws java.io.IOException if something goes wrong
     */
    public int readBytes(byte[] data, int size) throws IOException;

    /**
     * Read a single byte of data from the buffer
     *
     * @return the int value of the byte or -1 if the end of the data was reached.
     * @throws java.io.IOException if something goes wrong
     */
    public int readByte() throws IOException;

    /**
     * Reset the Reader and rewinds it to the begin.
     * This is useful to re-read the same buffer without instantiate a new reader on it..
     */
    public void reset() throws IOException;

    /**
     * Returns the whole buffer into a byte array.
     * WARNING! This method could be memory hungry if used with large data buffers.
     *
     * @return the buffer content.
     */
    public byte[] getWholeBufferAsByteArray() throws IOException;

    /**
     * Returns the size (in bytes) of the data stored into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    public long getSize();
}

package m.c.m.proxyma.buffers;

import java.io.IOException;

/**
 * <p>
 * This is a common interface for my Buffer classes.
 * This allows me to change the implementation as needed
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ByteBuffer.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public interface ByteBuffer {

    /**
     * Append the passed byte array to the buffer making an internal copy of it.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    public long appendBytes(byte[] data, int size) throws IOException, IllegalStateException;

    /**
     * Append the passed byte to the buffer.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    public long appendByte(int data) throws IOException, IllegalStateException;

    /**
     * Sets the buffer as ReadOnly.. no more data can be written into it.
     * If so, an IllegalStateException is raised.
     */
    public void lock();

    /**
     * Returns the size (in bytes) of the data stored into the buffer.
     *
     * @return the size of the buffer in bytes.
     */
    public long getSize();

    /**
     * check if the buffer is still writable
     *
     * @return the current status.
     */
    public boolean isLocked();

    /**
     * Returns a separate but inentical instance of the object
     * @return a clone of the object
     */
    public Object clone() throws CloneNotSupportedException;
}

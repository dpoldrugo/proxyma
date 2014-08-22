package m.c.m.proxyma.buffers;

import java.io.Serializable;
import java.io.IOException;

/**
 * <p>
 * This class implements a generalyzed Buffer that can be used to store large and small binary data.
 * It uses both a RamBuffer and a FileBuffer to mantain the data.
 * If the data that comes into the buffer are more than its soft-limit, the buffer
 * will store the exceding data into a temporary file to prevent high memory consumption.
 * You can't write simultaneouusly from separate threads (write operations are not thread-safe).
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p><p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * @version $Id: SmartBuffer.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class SmartBuffer implements Serializable, Cloneable, ByteBuffer {
    /**
     * Create a smart buffer that can countain into RAM a maximum of 256Kb of data.
     * If the data that comes into the buffer are more than this limit, the buffer
     * will store the exceding data into a temporary file.
     */
    public SmartBuffer() {
        this.maxSize = 262144; //256Kb
        this.ramBuf = new RamBuffer(RamBuffer.SMALL_BINARY_DATA);
    }

    /**
     * Create a smart buffer that can countain into RAM a specific amount of data.
     * If the data that comes into the buffer are more than this limit, the buffer
     * will store the exceding data into a temporary file.
     *
     * @param maxRamSize
     */
    public SmartBuffer(int maxRamSize) {
        this.maxSize = maxRamSize;
        if (maxRamSize <= 65536) // size < 64 Kb
            this.ramBuf = new RamBuffer(RamBuffer.TEXT_DATA);
        else if (maxRamSize <= 262144) // 64 Kb < size < 256 Kb
            this.ramBuf = new RamBuffer(RamBuffer.SMALL_BINARY_DATA);
        else // size > 256 Kb
            this.ramBuf = new RamBuffer(RamBuffer.LARGE_BINARY_DATA);
    }


    /**
     * Append the passed byte array to the buffer.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    @Override
    public long appendBytes(byte[] data, int toAppend) throws IOException, IllegalStateException {
        if (!locked) {
            if (!ramLimitExcedded && (this.size+toAppend) > maxSize) {
                //ram buffer limit reached fill it ans swithc to filebuffer.
                int intoRambuf = (int)(maxSize - this.size);
                toAppend -= intoRambuf;
                ramBuf.appendBytes(data, intoRambuf);
                this.size += intoRambuf;
                ramLimitExcedded = true;
                ramBuf.lock();

                //done with the ram buffer, now putting remaining data into file buffer
                fileBuf = new FileBuffer();
                for (int i=0; i< toAppend; i++)
                    fileBuf.appendByte(data[intoRambuf+i]);
            } else {
                if (ramLimitExcedded)
                    fileBuf.appendBytes(data, toAppend);
                else
                    ramBuf.appendBytes(data, toAppend);
            }
        } else {
            throw new IllegalStateException ("This buffer is locked.");
        }
        return (this.size += toAppend);
    }


    /**
     * Append the passed byte to the buffer.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    @Override
    public long appendByte(int data) throws IOException, IllegalStateException {
        if (!locked) {
            this.size++;
            if (!ramLimitExcedded && this.size > maxSize) {
                fileBuf = new FileBuffer();
                ramLimitExcedded = true;
                ramBuf.lock();
            }

            if (ramLimitExcedded)
                fileBuf.appendByte(data);
            else
                ramBuf.appendByte(data);
        } else {
            throw new IllegalStateException ("This buffer is locked.");
        }
        return this.size;
    }

    /**
     * Sets the buffer as ReadOnly.. no more data can be written into it.
     * If so, an IllegalStateException is raised.
     */
    @Override
    public void lock() {
        ramBuf.lock();
        if (fileBuf != null)
            fileBuf.lock();
        locked = true;
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
     * return false if the buffer is still appendable
     *
     * @return the current status.
     */
    @Override
    public boolean isLocked() {
        return locked;
    }

    /**
     * This method clones the current object.<br/>
     * Because it uses the same buffers of its parents as storage area,
     * the new clone will be locked. <br/>
     * ..in other words the clone can't be modified.
     *
     * @return a new and separate instance of the object.
     * @throws CloneNotSupportedException if the clone operation is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SmartBuffer clone = (SmartBuffer)super.clone();
        clone.ramBuf = (RamBuffer)ramBuf.clone();
        if (fileBuf != null)
            clone.fileBuf = (FileBuffer)fileBuf.clone();
        clone.locked = true;
        return clone;
    }

    /**
     * Obtain the internal FileBuffer to read into it.
     * @return the filebuffer.
     */
    protected FileBuffer getFileBuffer () {
        return this.fileBuf;
    }


    /**
     * Obtain the internal RamBuffer to read into it.
     * @return the filebuffer.
     */
      protected RamBuffer getRamBuffer () {
        return this.ramBuf; 
    }

    //Maximum size for the RamBuffer
    private int maxSize;

    //Size of the buffer
    private long size = 0;

    //The internal RamBuffer to store a small amount of data
    private RamBuffer ramBuf = null;

    //The internal FileBuffer to store large amount of data
    private FileBuffer fileBuf = null;

    //Flag to set if the limit was break when writing on the buffer.
    private boolean ramLimitExcedded = false;

    //Flag to set if the object is cached
    private boolean locked = false;
}

package m.c.m.proxyma.buffers;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.IOException;

/**
 * <p>
 * This class implements a ByeBuffer that can be used to store large (and small) binary data into RAM.
 * It uses a list of "pages" (byte array of a specified size) to mantain the data, and it's capable
 * of self-expansion when needed.
 * To avoid memory loss you should choose a proper value for the size of the pages.
 * A right value will be useful to optimize the use of the ram and to avoid internal fragmentation.
 * I have added some "esay constructors" that sets page size based upon the type of data that
 * you may want to store or based upon the size of the data to store.
 *
 * Note: you can read the data whenever you want with a RamBufferReader.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * @version $Id: RamBuffer.java 138 2010-06-20 13:53:32Z marcolinuz $
 */

/**
 * This class is a custom buffer to read and write byte data into RAM
 */
public class RamBuffer implements Serializable, Cloneable, ByteBuffer {

    /**
     * This constructor builds a new RamBuffer setting the pagesize to the
     * passed valut and initializes all internal attributes.
     *
     * @param pageSize the size of single a page (it can have the same size of the data to store).
     */
    public RamBuffer(int pageSize) {
        buffer = new ArrayList();
        this.pageSize = pageSize;
        byte[] page = new byte[pageSize];
        buffer.add(page);
        currInputpage = page;
    }

    /**
     * Append the passed byte array to the buffer making an internal copy of it.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    @Override
    public long appendBytes(byte[] data, int size) throws IOException, IllegalStateException {
        int count = 0;
        if (!locked) {
            if (size > data.length) {
                throw new IOException("Size of data can't be greater than the array's size.");
            } else {
                while (count < size) {
                    for (; (inputPtr < pageSize) && (count < size); inputPtr++, count++) {
                        currInputpage[inputPtr] = data[count];
                    }

                    if ((inputPtr == pageSize) && (count < size)) {
                        //I have to allocate a new page
                        currInputpage = allocatepage();
                    }
                }
            }
            this.size += count;
        } else {
            throw new IllegalStateException ("This buffer is locked.");
        }
        return this.size;
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
            if ((inputPtr == pageSize)) {
                //I have to allocate a new page
                currInputpage = allocatepage();
            }
            currInputpage[inputPtr] = (byte) data;
            size++;
            inputPtr++;
        } else {
            throw new IllegalStateException ("This buffer is locked.");
        }
        return size;
    }

    /**
     * Sets the buffer as ReadOnly.. no more data can be written into it.
     * If so, an IllegalStateException is raised.
     */
    @Override
    public void lock() {
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
     * Obtain the current page size for this buffer
     *
     * @return the page size
     */
    public int getpageSize() {
        return pageSize;
    }

    /**
     * This method clones the current object.<br/>
     * Because it uses the same buffer of its parent as storage area,
     * the new clone will be locked. <br/>
     * ..in other words the clone can't be modified.
     *
     * @return a new and separate instance of the object.
     * @throws CloneNotSupportedException if the clone operation is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        RamBuffer clone = (RamBuffer)super.clone();
        clone.locked = true;
        return clone;
    }

    /**
     *  Get the specified page of data
     *
     * @param pageNumber the number of the wanted page
     * @return the wanted page
     */
    protected byte [] getPage(int pageNumber) {
        return (byte [])buffer.get(pageNumber);
    }

    /**
     * Expand the buffer by allocating a new empty page and returns it.
     *
     * @return the new page.
     */
    private byte[] allocatepage() {
        byte[] newpage = new byte[pageSize];
        buffer.add(newpage);
        inputPtr = 0;
        return newpage;
    }

    //The main container of the data
    private List buffer;

    //The size of the data into the buffer
    private long size = 0;

    //The size of a single page
    private int pageSize;

    //The pointers to the current pages
    private byte[] currInputpage;

    //the pointers to the current page number and posiztion
    private int inputPtr = 0;

    //Flag to set if the object is cached
    private boolean locked = false;

    //public constants
    public static final int TEXT_DATA = 1024; //1kb
    public static final int SMALL_BINARY_DATA = 2048; //2kb
    public static final int LARGE_BINARY_DATA = 4096; //4kb
}

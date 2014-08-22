package m.c.m.proxyma.buffers;

import java.io.*;

/**
 * <p>
 * This class implements a ByteBuffer that can be used to store large and small binary data.
 * It uses a temporary file to mantain the data.
 * You can't write simultaneouusly from separate threads (write operations are not thread-safe).
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * @version $Id: FileBuffer.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class FileBuffer implements Serializable, Cloneable, ByteBuffer {
    /**
     * Create a file buffer over a temporary file.
     */
    public FileBuffer() throws IOException {
        buffer = File.createTempFile("Proxyma.FileBuffer-", ".raw");
        if (!buffer.canWrite())
            throw new IOException("Can't write into temporary file \"" + buffer.getName() + "\"");

        fileFullPath = buffer.getAbsolutePath();

        //I don't use a buffered output stream because this is a "file buffer"
        os = new FileOutputStream(buffer);
    }

    /**
     * Append the passed byte array to the buffer storing data into the temporary file.
     *
     * @param data a byte array that countains the data to store
     * @param size the number of bytes to copy.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    @Override
    public long appendBytes(byte[] data, int size) throws IOException, IllegalStateException {
        if (!locked) {
            os.write(data, 0, size);
            this.size += size;
        } else {
            throw new IllegalStateException ("This buffer is locked.");
        }
        return this.size;
    }

    /**
     * Append the passed byte to the file.
     *
     * @param data an integer that rappresents the byte data.
     * @return the total size of the buffer (total number of introduced bytes).
     * @throws IOException, IllegalStateException
     */
    @Override
    public long appendByte(int data) throws IOException, IllegalStateException {
        if (!locked) {
            os.write(data);
            size++;
        }else {
            throw new IllegalStateException ("This buffer is locked.");
        }
        return size;
    }
   
    /**
     * Sets the buffer as ReadOnly an close the output stream on it..
     * no more data can be written into this buffer.
     * If so, an IllegalStateException is raised.
     */
    @Override
    public void lock() {
        locked = true;

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os = null;
        }

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
     * Obtain the full path of the file with the data
     *
     * @return the full path of the file
     */
    public String getFileFullPath() {
        return fileFullPath;
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
     * Because it uses the same file of its parent as storage area,
     * the new clone will be locked. <br/>
     * ..in other words the clone can't be modified.
     *
     * @return a new and separate instance of the object.
     * @throws CloneNotSupportedException if the clone operation is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        FileBuffer clone = (FileBuffer)super.clone();
        clone.locked = true;
        clone.os = null;
        clone.buffer = null;
        return clone;
    }

    /**
     * close and delete the temporary file..
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (os != null)
                os.close();
            buffer.delete();
        } finally {
            super.finalize();
        }
    }

    //The file that rappresents the buffer
    private File buffer = null;

    //The output stream to write data into the buffer
    private OutputStream os = null;

    //Flag to set if the object is cached (useless in this implementation)
    private boolean locked = false;

    //File name and path
    private String fileFullPath = null;

    //The size of the stored data.
    private long size = 0;
}

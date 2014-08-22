package m.c.m.proxyma.buffers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * This class is the factory for the ByteBuffers implementations and allows
 * the users to change the buffers implementation as needed.<br>
 * Note: The configration is valid for a single context.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ByteBufferFactory.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class ByteBufferFactory {

    /**
     * Creates a new instance of a ByteBuffer implementation.<br/>
     * The specific implementation is choosen from the configration of the context.
     *
     * @param context the context where to find the configuration data.
     * @return a new ByteBuffer
     * @throws IllegalArgumentException if the class is not a ByteBuffer implementation
     * @throws ClassNotFoundException if the class is not found
     * @throws InstantiationException if the object can't be instantiated
     * @throws IllegalAccessException if there is an access violation.
     */
    public static ByteBuffer createNewByteBuffer(ProxymaContext context) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (byteBufferConstructor == null)
            initialize(context);
        
        ByteBuffer retVal;

        Object theBuffer = byteBufferConstructor.newInstance();
        if (!(theBuffer instanceof ByteBuffer)) {
            log.severe("The Class \"" + byteBufferConstructor.getName()  + "\" is not a ByteBuffer constructor!");
            throw new IllegalArgumentException("The Class \"" + byteBufferConstructor.getName()  + "\" is not a ByteBuffer constructor!");
        } else {
            retVal = (ByteBuffer)theBuffer;
        }

        return retVal;
    }

    /**
     * Creates a new instance of a ByteBufferReader implementation.<br/>
     * The specific implementation is choosen from the configration of the context.
     *
     * @param context the context where to find the configuration data.
     * @return a new ByteBuffer
     * @throws IllegalArgumentException if the class is not a ByteBuffer implementation
     * @throws ClassNotFoundException if the class is not found
     * @throws InstantiationException if the object can't be instantiated
     * @throws IllegalAccessException if there is an access violation.
     */
    public static ByteBufferReader createNewByteBufferReader(ByteBuffer theDataToRead) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ByteBufferReader retVal;

        Object theReader = byteBufferReaderConstructor.newInstance(theDataToRead);
        if (!(theReader instanceof ByteBufferReader)) {
            log.severe("The Class \"" + byteBufferReaderConstructor.getName() + "\" is not a ByteBufferReader constructor!");
            throw new IllegalArgumentException("The Class \"" + byteBufferReaderConstructor.getName() + "\" is not a ByteBufferReader constructor!");
        } else {
            retVal = (ByteBufferReader)theReader;
        }

        return retVal;
    }

    /**
     * Thread safely initialize this calss.
     * @param context the contex where to get the configuration data.
     */
    private static synchronized void initialize(ProxymaContext context) throws ClassNotFoundException, NoSuchMethodException {
        Class byteBuffer = Class.forName(context.getSingleValueParameter(ProxymaTags.GLOBAL_BUFFERS_IMPLEMENTATION+"/@writerClass"));
        Class byteBufferReader = Class.forName(context.getSingleValueParameter(ProxymaTags.GLOBAL_BUFFERS_IMPLEMENTATION+"/@threadSafeReaderClass"));
        byteBufferConstructor = byteBuffer.getConstructor();
        byteBufferReaderConstructor = byteBufferReader.getConstructor(byteBuffer);
        log = context.getLogger();
    }

    /**
     * The new implementation to build every time.
     */
    private static Constructor byteBufferConstructor = null;

    /**
     * The new implementation to build every time.
     */
    private static Constructor byteBufferReaderConstructor = null;

    /**
     * The logger for this class
     */
    private static Logger log = null;
}

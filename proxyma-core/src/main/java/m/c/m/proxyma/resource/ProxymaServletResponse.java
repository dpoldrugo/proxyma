package m.c.m.proxyma.resource;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxymaContext;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class implements a response wrapper.
 * It adapts servlet container response to be managed by Proxyma Serializers.
 * it is the only class that "knows" how to send the data back to the client.
 * Through this class Proxyma can transparently act as a standard Servlet.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaServletResponse.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class ProxymaServletResponse extends ProxymaResponse {

    /**
     * Default constructor for this class.
     * It thakes the original servlet response to wrap as parameter.
     * @param aResponse the HttpServletResponse to wrap
     */
    public ProxymaServletResponse (HttpServletResponse aResponse, ProxymaContext theContext) throws NullArgumentException {
        if (aResponse == null) {
            log.severe("I can't build a ProxymaResponse without an HttpServletResponse.");
            throw new NullArgumentException("I can't build a ProxymaResponse without an HttpServletResponse.");
        }
        this.theApplicationServerResponse = aResponse;
        this.log = theContext.getLogger();
    } 

    /**
     * This method uses the wrapped HttpServletResponse to send the whole
     * response data (headers, status, cookies and binary data) to the
     * Client.<br/>
     * Note: it performs the suggested operations specified in the ProxymaRequest documentation
     * @see ProxymaRequest
     * @return the status code of the response
     * @throws IllegalStateException if the data as been already sent to the client.
     */
    @Override
    public int sendDataToClient() throws IllegalStateException, IOException {
        //Checking if the response has been already sent.
        if (hasBeenSent()) {
            log.warning("This respone has been already sent to the client.");
            throw new IllegalStateException("This respone has been already sent to the client.");
        }
        else {
            log.finer("Start sending the response to the client..");
            sendingData();
        }

        /* Get the response data and do the right thing */
        ProxymaResponseDataBean responseData = getResponseData();
        int statusCode;
        if (responseData == null) {
            log.warning("Response data are \"null\"!");
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        } else {
            statusCode = serializeAndSendResponseData(responseData, this.theApplicationServerResponse);
        }
        
        return statusCode;
    }

    /**
     * This private method serilizes the response data into the passed http response.
     *
     * @param responseData the data to send
     * @param theResponse the response implementation to use to send the data
     * @return the status code of the operation.
     */
    private int serializeAndSendResponseData (ProxymaResponseDataBean responseData, HttpServletResponse theResponse) {
        //set the returnCode
        int exitStatus = responseData.getStatus();
        theResponse.setStatus(exitStatus);

        //set all the headers of the response data into the http servlet response..
        log.finer("Sending headers..");
        Iterator<String> stringIterarot = responseData.getHeaderNames().iterator();
        String headerName = null;
        ProxymaHttpHeader header = null;
        Collection<ProxymaHttpHeader> multiHeader = null;
        while (stringIterarot.hasNext()) {
            headerName = stringIterarot.next();
            if (responseData.isMultipleHeader(headerName)) {
                //Process multiple values header.
                multiHeader = responseData.getMultivalueHeader(headerName);
                Iterator<ProxymaHttpHeader> headers = multiHeader.iterator();
                while (headers.hasNext()) {
                    header = headers.next();
                    theResponse.setHeader(header.getName(), header.getValue());
                }
            } else {
                //Process Sungle value header
                header = responseData.getHeader(headerName);
                theResponse.setHeader(header.getName(), header.getValue());
            }
        }

        //set the cookies into the http servlet response.
        log.finer("Sending cookies..");
        Iterator<Cookie> cookieIterator = responseData.getCookies().iterator();
        while (cookieIterator.hasNext()) {
            theResponse.addCookie(cookieIterator.next());
        }

        //Serialize the data of the ByteBuffer into the servlet response..
        if (responseData.getData() != null) {
            BufferedOutputStream bos = null;
            log.finer("Sending data..");
            try {
                bos = new BufferedOutputStream(theResponse.getOutputStream());
                ByteBufferReader data = ByteBufferFactory.createNewByteBufferReader(responseData.getData());
                byte[] buffer = new byte[WRITE_BUFFER_SIZE];
                int count;
                while ((count = data.readBytes(buffer, WRITE_BUFFER_SIZE)) >= 0)
                    bos.write(buffer, 0, count);
            } catch (Exception e) {
                log.severe("Error in writing buffer data into the response!");
                e.printStackTrace();
                exitStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            } finally {
                try {
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                    }
                } catch (IOException e) {
                    log.severe("Error closing response output buffer!");
                    e.printStackTrace();
                    exitStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                }
            }
        }
        return exitStatus;
    }

    /**
     * The Application server's http response wrapped by this class.
     */
    private HttpServletResponse theApplicationServerResponse = null;

    /**
     * The logger for this response
     */
    private Logger log = null;

    /**
     * Size of the buffer to write data into the http response.
     */
    private static final int WRITE_BUFFER_SIZE = 1024; //these are bytes (1Kb)
}

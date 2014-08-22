package m.c.m.proxyma.resource;

import java.io.IOException;

/**
 * <p>
 * This is the "Adapter" abstract class that will be used by Proxyma to manage
 * client responses.<br/>
 * Any concrete class that extends this one can be used by Proxyma to send Client responses.
 * Through this class Proxyma can transparently handle  "Servlet" or
 * "Portlet" responses in the same way.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaResponse.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public abstract class ProxymaResponse {

    /**
     * Set the new ResponseDataBean overwriting any previous value.
     * In other words, this method changes the whole data of the response.
     * <br/>
     * Note: if this attribute is null no data will be sent back to the client.
     * @param responseData the new data for the response.
     * @see ProxymaResponseDataBean
     */
    public void setResponseData(ProxymaResponseDataBean responseData) {
        this.responseData = responseData;
    }

    /**
     * Returns the ResponseDataBEan that countains all the data of the response.
     * This method is used by plugins, cache providers and serializers to perform
     * their job over the data that will be sent back to the client.
     * @return the bean countaining the response data.
     * @see ProxymaResponseDataBean
     */
    public ProxymaResponseDataBean getResponseData() {
        return this.responseData;
    }

    /**
     * Return the value of the already-sent flag.<br/>
     * This method should be used by any implementation of "sendDataToClient"
     * to check if the data has already been sent.
     *
     * @return true or false.
     */
    public boolean hasBeenSent() {
        return this.alreadySent;
    }

    /**
     * Sets the value of the already-sent flag to "true"<br/>
     * This method should be used by any implementation of "sendDataToClient"
     * to declare the response as sent.
     */
    public void sendingData() {
        this.alreadySent = true;
    }

    /**
     * Serializes and sends the data of the response to the client.<br/><br/>
     * NOTE: As soon as starts, any implementation of this method HAVE to check if
     * the response has been already sent (using hasBeenSent() method) and rise
     * an IllegalStateException if it returns "true".<br/>
     * If the response is not already sent, then the implementation HAVE run
     * the sendingData() method before perform any job.
     * @return an interger value that rappresents the exit status of the operations.
     * @throws IllegalStateException if the hasBeenSent method-call returns "true".
     */
    public abstract int sendDataToClient() throws IllegalStateException, IOException;

    /**
     * The data-object that countains the response for the client.
     */
    private ProxymaResponseDataBean responseData = null;

    /**
     * If set to true any changes to the response are denyed
     */
    private boolean alreadySent = false;
}

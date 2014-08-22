package m.c.m.proxyma.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.buffers.ByteBuffer;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class rappresents all the data that can be sent to a Client as Response.<br/>
 * It is designed to be "rewritable" in any part allowing plugins to manipulate
 * it without any restriction.<br/>
 * This class makes Proxyma able to transparently handle "Servlet"
 * (and "Portlet"?) data in the same way.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaResponseDataBean.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class ProxymaResponseDataBean implements Cloneable, Serializable {
    /**
     * Returns a Collection of all the header names of the response.
     * If the response has no headers, this method returns an empty collection.
     * @return a collection of header names.
     */
    public Collection<String> getHeaderNames(){
        Iterator<String> iter = this.headers.keySet().iterator();
        LinkedList<String> retVal = new LinkedList<String>();
        while (iter.hasNext()) {
            retVal.add(getHeader(iter.next()).getName());
        }
        return retVal;
    }

    /**
     * This method is done to get values from a multiple values header
     * (like Cache-Control).
     * It returns a Collection of the specified response http headers.<br/>
     * If the wanted header is not found, this method returns null.<br/>
     * The header name is case insensitive. <br/>
     * You can use this method with any response header indeed, if the header
     * has a single value this method will return a collection with only one
     * element into it.
     *
     * @return the wanted header or null if the header doesn't exists
     * @see ProxymaHttpHeader
     */
    public Collection<ProxymaHttpHeader> getMultivalueHeader(String headerName) {
        Collection<ProxymaHttpHeader> retVal = null;
        Object value = this.headers.get(headerName.toLowerCase().trim());
        if (value instanceof ProxymaHttpHeader) {
            retVal = new LinkedList<ProxymaHttpHeader>();
            retVal.add((ProxymaHttpHeader)value);
        }
        else if (value instanceof LinkedList)
            retVal = (Collection<ProxymaHttpHeader>)value;
        return retVal;
    }

    /**
     * Returns the specified response http header.<br/>
     * If the wanted header is not found, this method returns null.<br/>
     * The header name is case insensitive. <br/>
     * You can use this method with any respose http header.<br/>
     * NOTE: if the header has multiple values this method will return only
     * the first.
     *
     * @return the wanted header or null if the header doesn't exists
     * @see ProxymaHttpHeader
     */
    public ProxymaHttpHeader getHeader(String headerName) {
        ProxymaHttpHeader retVal = null;
        Object value = this.headers.get(headerName.toLowerCase().trim());
        if (value instanceof ProxymaHttpHeader)
            retVal = (ProxymaHttpHeader)value;
        else if (value instanceof LinkedList)
            retVal = (ProxymaHttpHeader)((LinkedList)value).getFirst();
        return retVal;
    }

    /**
     * Adds an header to the response headers using the given name and value to create it.<br/>
     * This method allows to set multiple values for the same headerName.<br/>
     * You can use the containsHeader method if you want to test for the presence
     * of a an existing header before setting its value.
     * @throws NullArgumentException is raised if the header name is null
     * @see ProxymaHttpHeader
     */
    public void addHeader(String headerName, int headerValue) {
        String stringValue = Integer.toString(headerValue);
        addHeader(headerName, stringValue);
    }

    /**
     * Adds an header to the response headers using the given name and value to create it.<br/>
     * This method allows to set multiple values for the same headerName.<br/>
     * You can use the containsHeader method if you want to test for the presence
     * of a an existing header before setting its value.
     * @throws NullArgumentException is raised if the header name is null
     * @see ProxymaHttpHeader
     */
    public void addHeader(String headerName, long headerValue) {
        String stringValue = Long.toString(headerValue);
        addHeader(headerName, stringValue);
    }

    /**
     * Adds an header to the response headers using the given name and value to create it.<br/>
     * This method allows to set multiple values for the same headerName.<br/>
     * You can use the containsHeader method if you want to test for the presence
     * of a an existing header before setting its value.
     * @throws NullArgumentException is raised if the header name is null
     * @see ProxymaHttpHeader
     */
    public void addHeader(String headerName, String headerValue) throws NullArgumentException {
        if (headerName == null)
            throw new NullArgumentException("You can't set a null-named header");

        //Evaulate if the Map already countains the header.
        String lowercaseHeaderName = headerName.toLowerCase().trim();
        if (this.headers.containsKey(lowercaseHeaderName)) {
            Object existingValue = this.headers.get(lowercaseHeaderName);
            if (existingValue instanceof ProxymaHttpHeader) {
                //A single value already exists
                LinkedList<ProxymaHttpHeader> newValue = new LinkedList<ProxymaHttpHeader>();
                newValue.add((ProxymaHttpHeader)existingValue);
                newValue.add(new ProxymaHttpHeader(headerName, headerValue));
                this.headers.put(lowercaseHeaderName, newValue);
            }
            else if (existingValue instanceof LinkedList) {
                //Multiple values already exists
                ((LinkedList<ProxymaHttpHeader>)existingValue).add(new ProxymaHttpHeader(headerName, headerValue));
            }
        } else {
            //The header doesn't exists
            this.headers.put(lowercaseHeaderName, new ProxymaHttpHeader(headerName, headerValue));
        }
    }

    /**
     * Checks whether the response has already an header with the specified name.
     *
     * @param headerName The name of the header to check
     * @return true if the header is found
     */
    public boolean containsHeader(String headerName) {
        return this.headers.containsKey(headerName.toLowerCase().trim());
    }

    /**
     * Checks whether the specified header has multiple values.
     *
     * @param headerName The name of the header to check
     * @return true if the header has multiple values
     * @throws NullPointerException if the header doesn't exists at all.
     */
    public boolean isMultipleHeader(String headerName) throws NullPointerException {
        boolean retVal = false;
        Object theHeader = this.headers.get(headerName.toLowerCase().trim());
        if (theHeader == null)
            throw new NullPointerException("Attempting to check the multiplicity of an unexisting header: " + headerName);
        else if (theHeader instanceof LinkedList)
            retVal = true;
        return retVal;
    }

    /**
     * Removes an header from the response data.<br/>
     * The header name is case insensitive<br/>
     * If the specifyed header is not found, nothing will be done.<br/>
     * Please use the containsHeader method to check for the presence of an header before remove it.<br/>
     * Note: this method will remove any instnance of the specified header, so all
     * the values of a multiple-values header will be removed.
     *
     * @param headerName The name of the header to remove
     */
    public void deleteHeader(String headerName) {

        String lowercaseHeaderName = headerName.toLowerCase().trim();
        if (this.headers.containsKey(lowercaseHeaderName))
            this.headers.remove(lowercaseHeaderName);
    }

    /**
     * Returns a collection of all of the Cookie objects of the response.
     * This method returns an empty collecion if there are no cookies.
     * @return a Collection of cookies
     *
     */
    public Collection<Cookie> getCookies() {
        return this.cookies.values();
    }

    /**
     * Returns the specified cookie.
     * If the cookie is not found, this method returns null.
     *
     * @return the header value
     */
    public Cookie getCookie(String cookieName) {
        return this.cookies.get(cookieName);
    }

    /**
     * Adds a Cookie to the response.
     * If the Cookie has been already set, the new value overwrites the previous one.
     * The containsCookie method can be used to test for the presence of a
     * Cookie before setting its value.
     * @throws NullArgumentException if the passed argument is null
     */
    public void addCookie(Cookie aCookie) throws NullArgumentException {
        if (aCookie == null)
            throw new NullArgumentException("You can't set a null Cookie");

        this.cookies.put(aCookie.getName(), aCookie);

    }

    /**
     * Checks whether the response has already a Cookie with the specified name.
     *
     * @param cookieName The name of the Cookie to check
     * @return true if the cookie was found
     */
    public boolean containsCookie(String aCookieName) {
        return this.cookies.containsKey(aCookieName);
    }

    /**
     * Removes the Cookie with the given name.<br/>
     * If the Cookie is not found, nothing is done.<br/>
     * You can always use the containsCookie method to test for the presence
     * of a Cookie before remove it.
     *
     * @param cookieName The name of the cookie to remove
     * @throws NullArgumentException if the passed parameter is null
     */
    public void deleteCookie(String cookieName) throws NullArgumentException {
        if (cookieName == null)
            throw new NullArgumentException("You can't delete a null Cookie");
        this.cookies.remove(cookieName);
    }

    /**
     * Get the raw binary data of the response
     * @return a ByteBuffer containing the raw data of the resource
     */
    public ByteBuffer getData() {
        return this.data;
    }

    /**
     * Set the raw binary data of the response
     * @param aBuffer the ByteBuffer containing the binary data
     *
     */
    public void setData(ByteBuffer aBuffer) {
         //Throw an exception if the resource is locked
        this.data = aBuffer;
    }

    /**
     * Get the current value of the status code of the response
     * @return the current status
     */
    public int getStatus () {
        return this.status;
    }

    /**
     * Sets the status code for this response.
     * This method is used to set the return status code when there is no error
     * (for example, for the status codes SC_OK or SC_MOVED_TEMPORARILY).
     *
     * @param value the new status value
     * @see serializeToClient
     */
    public void setStatus (int value) {
        this.status = value;
    }

    /**
     * Returns the size in bytes of the raw binary data stored into the response<br/>
     * ..or in other words, the value to set for the Content-Length header.
     * @return the size of the response binary data.
     */
    public long getContentLenght() {
        return this.data.getSize();
    }

    /**
     * This method clone the current response data into a new separated object.
     *
     * @return a new and separate instance of the object.
     * @throws CloneNotSupportedException if the clone operation is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        //clone self
        ProxymaResponseDataBean clone = (ProxymaResponseDataBean)super.clone();

        //Clone headers
        clone.headers = (HashMap<String, Object>) new HashMap();
        Iterator<String> stringIterarot = this.getHeaderNames().iterator();
        String headerName = null;
        ProxymaHttpHeader header = null;
        Collection<ProxymaHttpHeader> multiHeader = null;
        while (stringIterarot.hasNext()) {
            headerName = stringIterarot.next();
            if (this.isMultipleHeader(headerName)) {
                //Process multiple values header.
                multiHeader = this.getMultivalueHeader(headerName);
                Iterator<ProxymaHttpHeader> instanceHeaders = multiHeader.iterator();
                while (instanceHeaders.hasNext()) {
                    header = instanceHeaders.next();
                    clone.addHeader(header.getName(), header.getValue());
                }
            } else {
                //Process Sungle value header
                header = this.getHeader(headerName);
                clone.addHeader(header.getName(), header.getValue());
            }
        }

        //clone cookies
        clone.cookies = (HashMap<String, Cookie>) new HashMap();
        Iterator<Cookie> cookieIterator = this.getCookies().iterator();
        while (cookieIterator.hasNext()) {
            Cookie original = cookieIterator.next();
            clone.addCookie((Cookie)original.clone());
        }

        //Clone data
        if (data != null)
            clone.data = (ByteBuffer)data.clone();
        return clone;
    }

    /**
     * The collection of the available headers of the response
     */
    private HashMap<String, Object> headers = new HashMap();

    /**
     * The Map with the avilable cookies of the response
     */
    private HashMap<String, Cookie> cookies = new HashMap();

    /**
     * The return status of the response
     */
    private int status = HttpServletResponse.SC_ACCEPTED;

    /**
     * The binary data of the response
     */
    private ByteBuffer data = null;
}

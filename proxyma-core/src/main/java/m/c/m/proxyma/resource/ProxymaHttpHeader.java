package m.c.m.proxyma.resource;

import java.io.Serializable;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class rappresents a single Http Header that can be sent to a Client.<br/>
 * It is designed to be "rewritable", so you can get it and update its value as
 * much as needed.<br/>
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaHttpHeader.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class ProxymaHttpHeader implements Cloneable, Serializable {
    /**
     * The default constructor for this class.<br/>
     * It doesnt accept a null value as name.<br/>
     * NOTE: It accepts a null value as "value" but will store an empty string.
     * 
     * @param name the header name
     * @param value the value of the header
     * @throws NullArgumentException if the name is null
     */
    public ProxymaHttpHeader (String name, String value) throws NullArgumentException {
        if (name == null || name.trim().equals(""))
            throw new NullArgumentException("I can't build an HttpHeader with a null or empty name.");
        this.name = name.trim();
        if (value == null)
            this.value = "";
        else
            this.value = value.trim();
    }

    /**
     * Returns the name of the header.
     * @return the header name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the header
     * @return the header value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets a new value for the header.
     * @param newValue the new value to set
     */
    public void setValue(String newValue) {
        if (newValue == null)
            this.value = "";
        else
            this.value = newValue.trim();
    }

    /**
     * This overrides the Object's toString method.<br/>
     * It's done to make the Header able to auto serialize itself into a string
     * ready to be sent to the client.
     * @return
     */
    @Override
    public String toString() {
        StringBuffer retVal = new StringBuffer();
        retVal.append(name);
        retVal.append(": ");
        retVal.append(value);
        return retVal.toString();
    }

    /**
     * This method clone the current header.
     *
     * @return a new and separate instance of the object.
     * @throws CloneNotSupportedException if the clone operation is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ProxymaHttpHeader clone = (ProxymaHttpHeader)super.clone();
        return clone;
    }

    /**
     * The name of the header
     */
    private String name = null;

    /**
     * The value of the header
     */
    private String value = null;
}

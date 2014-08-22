package m.c.m.proxyma.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 *
 * This class rappresents a resource managed by the proxy.<br/>
 * It countains a request and a response and it has all the attributes required
 * by the proxyma-core to manage a client request/response.<br/>
 * In other words, this is the object that will be managed by all the plugins.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaResource.java 176 2010-07-03 09:02:14Z marcolinuz $
 */
public class ProxymaResource {
    /**
     * Default constructor for this class
     *
     * @param context the context where the Resource will live.
     * @throws NullArgumentException if any of the passed parameters is null
     */
    public ProxymaResource (ProxymaRequest request, ProxymaResponse response, ProxymaContext context)
           throws NullArgumentException {
        //initialize the logger for this class.
        log = context.getLogger();

        if (context == null) {
            log.severe("Null context passed to the constructor.");
            throw new NullArgumentException("Unable to create a resource from a null context");
        }
        if (request == null) {
            log.severe("Null request passed to the constructor.");
            throw new NullArgumentException("Unable to create a resource from a null request");
        }
        if (response == null) {
            log.severe("Null response passed to the constructor.");
            throw new NullArgumentException("Unable to create a response from a null response");
        }

        this.context = context;
        this.request = request;
        this.response = response;
        log.finer("Created new eesource for context " + context.getName());
    }

    /**
     * Returns the contex where this resource was generated.
     *
     * @return the current context for this resource.
     */
    public ProxymaContext getContext() {
       return this.context;
    }

    /**
     * Returns the request generated from the factory method.
     * It countains the client request.
     *
     * @return the client request
     */
    public ProxymaRequest getRequest() {
       return this.request;
    }

    /**
     * Returns the response generated from the factory method
     * This the response that will be sent back to the client
     *
     * @return the response for the client.
     */
    public ProxymaResponse getResponse() {
       return this.response;
    }

    /**
     * Returns the proxy foder that matched the requested URI.
     *
     * @return the response for the client.
     */
    public ProxyFolderBean getProxyFolder() {
       return this.folder;
    }

    /**
     * Returns the subpath relative to the destination of the proxy folder
     *
     * @return the subpath of the proxy folder
     */
    public String getDestinationSubPath() {
        return destinationSubPath;
    }

    /**
     * Sets the subpath relative to the destination of the proxy folder
     */
    public void setDestinationSubPath(String destinationSubPath) {
        this.destinationSubPath = destinationSubPath;
    }

    /**
     * Returns the complete URI where proxyma is deployed
     *
     * @return http://host[:port]/
     */
    public String getProxymaRootURLAsString() {
        return proxymaRootURL;
    }

    /**
     * Returns the URI where proxyma is deployed
     *
     * @return the URI where proxyma is deployed
     */
    public URL getProxymaRootURL() {
        URL retVal = null;
        try {
            retVal = new URL(proxymaRootURL);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ProxymaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal;
    }

    /**
     * Sets the complete URI where proxyma is deployed
     */
    public void setProxymaRootURI(String uri) {
        this.proxymaRootURL = uri;
    }

    /**
     * Set the proxy foder that matched the requested URI.
     *
     * @return the response for the client.
     */
    public void setProxyFolder(ProxyFolderBean theFolder) {
       if (theFolder == null) {
           log.warning("Attempt to register a Null proxy folder into a resource");
           throw new NullArgumentException("You can't set a null folder for a request.");
       }
       this.folder = theFolder;
    }

    /**
     * Get an attribute (if exists) from the resource using its name
     *
     * @param anAttributeName the wanted Resource Attribute
     * @return null if the attribute doesn't exists.
     */
    public Object getAttribute(String anAttributeName) {
       Object retVal = null;
       log.finer("Searching for attribute " + anAttributeName);
       if (anAttributeName == null) {
           log.warning("Null attribute name parameter.. Ignoring operation");
       } else if (attributes.containsKey(anAttributeName)) {
           retVal = attributes.get(anAttributeName);
       } else {
           log.finer("Attribute " + anAttributeName + " not found.");
       }
       return retVal;
    }

    /**
     * Add a new Attribute to the resource.
     * Note: any duplicated entry will be overwritten. Use the containsAttribute() method to check if you care about to not overwrite anything.
     *
     * @param attributeName the name of the attribute to add.
     * @param attributeValue the value of the attribute, it can be any kind of object so it's a responsability of the reader to do the upper-cast to the proper class.
     * @throws NullArgumentException if the attribute name is null
     */
    public void addAttibute(String attributeName, Object attributeValue) throws NullArgumentException {
        if (attributeName == null || attributeValue == null) {
            log.warning("Null attribute name or value parameter.. Ignoring operation");
            throw new NullArgumentException("Null attribute name parameter.. Ignoring operation");
        } else {
            boolean exists = attributes.containsKey(attributeName);
            if (exists) {
                log.finer("The attribute \"" + attributeName + "\" already exists.. overwriting it.");
                attributes.remove(attributeName);
            } else {
                log.finer("Adding new attribute " + attributeName);
            }
            attributes.put(attributeName, attributeValue);
        }
    }

    /**
     * Remove an attribute from the resource
     * @param attributeName the attribute to remove.
     * @throws NullArgumentException if the argument is null
     */
    public void deleteAttribute (String anAttributeName) throws NullArgumentException {
       if (anAttributeName == null) {
            log.warning("Null attribute parameter.. Ignoring operation");
            throw new NullArgumentException("Null ProxyFolderBean parameter.. Ignoring operation");
        } else {
            boolean exists = attributes.containsKey(anAttributeName);
            if (!exists) {
                log.finer("The attribute \"" + anAttributeName + "\" doesn't exists.. nothing done.");
            } else {
                log.finer("Deleting existing attribute " + anAttributeName);
                attributes.remove(anAttributeName);
            }
        }
    }

    /**
     * Returns a collection of attribute names stored into the resource
     * 
     * @return the attribute names
     */
    public Collection<String> getAttributeNames () {
        return attributes.keySet();
    }


    /**
     * The context of the resource
     */
    private ProxymaContext context = null;

    /**
     * The proxy folder that matches the client request
     */
    private ProxyFolderBean folder = null;

    /**
     * The client request
     */
    private ProxymaRequest request = null;

    /**
     * The client response that will be refined by the reverse proxy engine
     */
    private ProxymaResponse response = null;

    /**
     * The subpath relative to the destination of the proxy folder.
     */
    private String destinationSubPath = null;

    /**
     * The subpath relative to the destination of the proxy folder.
     */
    private String proxymaRootURL = null;

    /**
     * A general purpose container for intra-plugins communication using key-value pairs.
     */
    private Map<String,Object> attributes = new Hashtable();

    /**
     * The logger for this class
     */
    private Logger log = null;
}

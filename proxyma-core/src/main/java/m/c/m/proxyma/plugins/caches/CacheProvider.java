package m.c.m.proxyma.plugins.caches;

import java.util.Collection;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the interface to implement to provide a CacheProvider to the proxy engine.<br/>
 * The default implementation of it is made using ECHACHE an Open Source Cache Manager
 * but you can add any other cache engine if you want to.<br/>
 * IMPORTANT: The cache plugn is used 2 times into the proxy-engine:
 * <ul>
 *  <li>After the last preprocessor-plugin has run. In this way the engine can evaulate if there is a cached version of the response already available.</li>
 *  <li>Before start the the serializer-plugin to decide if the resource can be cached.</li>
 * </ul>
 * </p><p>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: CacheProvider.java 177 2010-07-03 09:08:10Z marcolinuz $
 */
public interface CacheProvider {
    /**
     * Method provided to check if the resource is cacheable and store it
     * into the cache subsystem.
     *
     * @param aResource the resource countaining the response to store.
     */
    public void storeResponseDataIfCacheable(ProxymaResource aResource);

    /**
     * Method provided to search into the cache a responseData suitable for the passed resource.
     *
     * @param aResource the resource to complete with a response.
     * If a fittin gresponse is found it will be attached to the resource.
     *
     * @return false if no fitting response were found into the cache.
     */
    public boolean getResponseData(ProxymaResource aResource);

    /**
     * Method provided to get a collection on URIs (keys for the cache matching) that are stored
     * into the cache subsystem.
     *
     * @return a collection of URIs
     */
    public Collection<String> getCachedURIs ();

    /**
     * Some cache subsystems keeps track of the opeations and maintains an
     * internal statistic chat can be queried.
     * This method is provided to let you use this feature if available.
     *
     * @return some statistics data about the cache status and usage.
     */
    public String getStatistics();

    /**
     * Returns the name of the cache provider (only a name that characterize the plugin,
     * not the class name..) that could be used into the interfaces as cache name.
     * @return the cache name.
     */
    public String getName();

    /**
     * Returns a short description (html formatted) of the cache.<br>
     * It will be used into the interfaces to give some information about the cache.
     *
     * @return a short description of the cache.
     */
    public String getHtmlDescription();
}

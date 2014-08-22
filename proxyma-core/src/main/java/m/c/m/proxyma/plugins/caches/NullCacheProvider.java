package m.c.m.proxyma.plugins.caches;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * </p><p>
 * This is a null implementation of the interface CacheProvider.
 * It doesn't stores anything and never returns anything.<br/>
 * IMPORTANT: The cache plugn is used 2 times into the engine:
 * <ul>
 *  <li>After the last preprocessor-plugin has run. In this way the engine can evaulate if there is a cached version of the response already available.</li>
 *  <li>Before start the the serializer-plugin to decide if the resource can be cached.</li>
 * </ul>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: NullCacheProvider.java 177 2010-07-03 09:08:10Z marcolinuz $
 */
public class NullCacheProvider implements m.c.m.proxyma.plugins.caches.CacheProvider {

    /**
     * The default constructor for all the plugins.
     * @param context the context where the plugin is instantiated
     */
    public NullCacheProvider (ProxymaContext context) {
        log = context.getLogger();
        log.info("Null cache initialized..");
    }

    /**
     * This method does nothing.
     * @param aResource
     */
    @Override
    public void storeResponseDataIfCacheable(ProxymaResource aResource) {
        log.finer("Null cache cant't store responses..");
    }

    /**
     * This method does nothing.
     * @param aResource a ProxymaResource
     * @return always false
     */
    @Override
    public boolean getResponseData(ProxymaResource aResource) {
        log.finer("Null cache cant't get any respones..");
        return false;
    }

    /**
     * This method does nothing.
     * @return always an empty Collection.
     */
    @Override
    public Collection<String> getCachedURIs() {
        log.finer("Null cache always returns an empty collection..");
        return new LinkedList();
    }

    /**
     * This method does nothing.
     * @return always the same message
     */
    @Override
    public String getStatistics() {
        log.finer("Null cache doesn't provide statistics..");
        return "Null cache doesn't provide statistics..";
    }

    /*
     * Returns the name of the cache provider.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of the cache provider formatted for html output.
     * @return the description of the cache provider
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * The name of this plugin.
     */
    private static final String name = "Null Cache Provider";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "To be honest, this is not a cache.<br/>" +
            "With this plugin you can disable the cache subsystem " +
            "for the current proxy-folder. In other words, it will force the proxy engine to always retrive the " +
            "resources from the original server.";
}

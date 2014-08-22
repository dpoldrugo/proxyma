package m.c.m.proxyma.plugins.caches;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

/**
 * </p><p>
 * This is a wrapper for the famous ecache cache subsystem.<br/>
 * It implements the CacheProvider interface to make Proxyma able to work with it.<br/>
 * IMPORTANT: The cache manager is not shared between different proxyma contexts.
 * </p><p>
 * Finally, the cache provider is used twice into the proxyma engine:
 * <ul>
 *  <li>After the last preprocessor-plugin has run ot evaluate if there is a cached version of the response available.</li>
 *  <li>Before start the the serializer-plugin to decide if the resource can be cached.</li>
 * </ul>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: EhcacheCacheProvider.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class EhcacheCacheProvider implements m.c.m.proxyma.plugins.caches.CacheProvider {

    /**
     * The default constructor for all the plugins.
     * @param context the context where the plugin is instantiated
     */
    public EhcacheCacheProvider(ProxymaContext context) {
        //initialize logger
        log = context.getLogger();

        //Initialize cache manager if it's not already done.
        if (ehCacheManager == null) {
            initializeCacheManager(context);
        }
        theCache = ehCacheManager.getCache(context.getName());

        //Set the maximum size of a cached resource
        String ecacheConfigurationsXPath = ProxymaTags.AVAILABLE_CACHE_PROVIDERS + "[@class='" + this.getClass().getName() + "']";
        maxCacheableResourceSize = Integer.parseInt(context.getSingleValueParameter(ecacheConfigurationsXPath + "/@maxSizeOfCacheableResource"));
    }

    /**
     * This method stores the data of a response into the cache subsystem using
     * as key the request URI and the QueryString.
     * @param aResource the resource that countains the data to store.
     */
    @Override
    public void storeResponseDataIfCacheable(ProxymaResource aResource) {
        ProxymaRequest theRequest = aResource.getRequest();
        if ((aResource.getResponse().getResponseData().getData() != null) &&  isCacheable(aResource.getResponse().getResponseData())) {
            log.finer("The resource is cacheable.. storing it into the cache");
            Element element = new Element(calculateKey(aResource), aResource.getResponse().getResponseData());
            theCache.put(element);
        } else {
            log.finer("The resource is not cacheable.. nothing done.");
        }
    }

    /**
     * This method searches into the cache subsystem using the URI and the QueryString
     * of the request countained into the passed resource.
     * @param aResource the resource that countains the data to perform the search.
     * @return true if the cache countains a resource that matches the requisites.
     */
    @Override
    public boolean getResponseData(ProxymaResource aResource) {
        boolean retValue = false;
        Element elem = theCache.get(calculateKey(aResource));
        if (elem != null) {
            aResource.addAttibute(CACHE_HIT_ATTRIBUTE, "Cache Hit!");
            aResource.getResponse().setResponseData((ProxymaResponseDataBean) elem.getValue());
            retValue = true;
        }
        return retValue;
    }

    /**
     * This method does nothing.
     * @return always an empty Collection.
     */
    @Override
    public Collection<String> getCachedURIs() {
        return theCache.getKeys();
    }

    /**
     * This method does nothing.
     * @return always the same message
     */
    @Override
    public String getStatistics() {
        Statistics ehcacheStats = theCache.getStatistics();
        StringBuffer stats = new StringBuffer();
        stats.append("---- Proxyma EhcacheProvider Plugin Statisctics ----");
        stats.append("\nCache instance Name: ");
        stats.append(theCache.getName());
        stats.append("\nCache status: ");
        stats.append(theCache.getStatus());
        stats.append("\nNumber of elements currently on the cache: ");
        stats.append(theCache.getSize());
        stats.append("\nNumber of elements into the Memory Store: ");
        stats.append(theCache.getMemoryStoreSize());
        stats.append("\nNumber of elements into the Disk Store: ");
        stats.append(theCache.getDiskStoreSize());
        stats.append("\nNumber of Cache HITS: ");
        stats.append(ehcacheStats.getCacheHits());
        stats.append("\nNumber of Cache MISS: ");
        stats.append(ehcacheStats.getCacheMisses());
        stats.append("\n---- End of EhcacheProvider Plugin Statisctics ----\n");
        return stats.toString();
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
     * Inspect the response Headers to understand if the resource can be
     * stored into the cache.
     *
     * @param responseData the response data to inspect
     * @return true if the response is cacheable.
     */
    private boolean isCacheable(ProxymaResponseDataBean responseData) {
        boolean cacheableFlag = false;

        //Check for intresting HTTP headers
        ProxymaHttpHeader pragmaHeader = responseData.getHeader(PRAGMA_HEADER);
        ProxymaHttpHeader cacheControlHeader = responseData.getHeader(CACHE_CONTROL_HEADER);

        //If there are no directives we can cache it
        if ((maxCacheableResourceSize > 0) && (responseData.getData().getSize() > maxCacheableResourceSize)) {
            cacheableFlag = false;
        } else if ((pragmaHeader == null) && (cacheControlHeader == null)) {
            //No directives found: we can cache it!!!
            cacheableFlag = true;
        } else {
            if (pragmaHeader != null)
            // check for pragma directives..
            if (pragmaHeader != null) {
                //Check the Pragma header..
                String pragmaHeaderValue = pragmaHeader.getValue();
                if (PRAGMA_NO_CACHE.equalsIgnoreCase(pragmaHeaderValue)) //we can't cache this...
                {
                    cacheableFlag = false;
                } else {
                    cacheableFlag = true;
                }
            }

            // check for cache-control directives
            if (cacheControlHeader != null) {
                String cacheControlHeaderValue = cacheControlHeader.getValue();
                //Check the value
                if ((cacheControlHeaderValue.indexOf(CACHE_CONTROL_NO_CACHE) >= 0)
                        || (cacheControlHeaderValue.indexOf(CACHE_CONTROL_NO_STORE) >= 0)
                        || (cacheControlHeaderValue.indexOf(CACHE_CONTROL_PRIVATE) >= 0)) {
                    //we can't cache this
                    cacheableFlag = false;
                } else if (cacheControlHeaderValue.indexOf(CACHE_CONTROL_PUBLIC) >= 0) {
                    cacheableFlag = true;
                } else {
                    //Ok.. we have to parse the whole string.. :O(
                    //we split the values by "," and search for max-age values.
                    String[] entries = cacheControlHeaderValue.split(",");
                    for (int i = 0; i < entries.length; i++) {
                        if (entries[i].indexOf(CACHE_CONTROL_MAX_AGE) >= 0) {
                            //find maxage value
                            int value = Integer.parseInt(entries[i].replaceFirst(CACHE_CONTROL_MAX_AGE, EMPTY_STRING));
                            if (value > 0) {
                                cacheableFlag = true;
                            } else {
                                cacheableFlag = false;
                            }
                            break;
                        } else if (entries[i].indexOf(CACHE_CONTROL_S_MAXAGE) >= 0) {
                            //find maxage value
                            int value = Integer.parseInt(entries[i].replaceFirst(CACHE_CONTROL_S_MAXAGE, EMPTY_STRING));
                            if (value > 0) {
                                cacheableFlag = true;
                            } else {
                                cacheableFlag = false;
                            }
                            break;
                        }
                    }
                }
            } else {
                //we can cache this
                cacheableFlag = true;
            }
        }

        //return the value of the ispection
        return cacheableFlag;
    }

    /**
     * Calculates the value to use as store key for the cache subsystem using
     * the values into the current resource.
     * @param aResource the resource to inspect
     * @return a string value that will be the search ckey for the reosurce into the cache.
     */
    private String calculateKey (ProxymaResource aResource) {
        ProxymaRequest request = aResource.getRequest();
        StringBuffer retVal = new StringBuffer(request.getRequestURI());
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if (request.getQueryString() != null)
                retVal.append("?").append(request.getQueryString());
        } else {
            Enumeration<String> parameters=request.getParameterNames();
            retVal.append("?");
            while (parameters.hasMoreElements()) {
                String parameterName = parameters.nextElement();
                retVal.append(parameterName);
                retVal.append("=");
                retVal.append(request.getParameter(parameterName));
                if (parameters.hasMoreElements())
                    retVal.append("&");
            }
        }
        return retVal.toString();
    }

    /**
     * Initialize the cache manager singleton of ecache.
     * @param context the proxyma context
     */
    private synchronized void initializeCacheManager(ProxymaContext context) {
        try {
            if (ehCacheManager == null) {
                ehCacheManager = new CacheManager(new ByteArrayInputStream(forgeEhcacheConfiguration(context)));
                ehCacheManager.addCache(context.getName());
                Cache addedCache = ehCacheManager.getCache(context.getName());
                addedCache.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_BEST_EFFORT);
                log.info("Ehcahe subsystem for context \"" + context.getName() + "\" initialized..");
            }
        } catch (UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, "Unable to initialize ehcache subsystem!", ex);
        }
    }

    /**
     * Forge a string to create an ehcache cachemanager using the configuration
     * provided by proxyma-config.xml.
     *
     * @param context the context were the cache will be active
     * @return a byte array that can be used as input for a byteArrayInputStream.
     * @throws UnsupportedEncodingException if the encoding of the string is no supported.
     */
    private byte[] forgeEhcacheConfiguration(ProxymaContext context) throws UnsupportedEncodingException {
        StringBuffer ehcacheConfig = new StringBuffer();
        String ecacheConfigurationsXPath = ProxymaTags.AVAILABLE_CACHE_PROVIDERS + "[@class='" + this.getClass().getName() + "']";

        ehcacheConfig.append("<ehcache>\n");
        ehcacheConfig.append("\t<diskStore path=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/diskStore/@path"));
        ehcacheConfig.append("\"/>\n");
        ehcacheConfig.append("\t<defaultCache maxElementsInMemory=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@maxElementsInMemory"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\teternal=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@eternal"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\ttimeToIdleSeconds=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@timeToIdleSeconds"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\ttimeToLiveSeconds=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@timeToLiveSeconds"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\toverflowToDisk=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@overflowToDisk"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\tdiskPersistent=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@diskPersistent"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\tdiskExpiryThreadIntervalSeconds=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@diskExpiryThreadIntervalSeconds"));
        ehcacheConfig.append("\"\n");
        ehcacheConfig.append("\t\tmemoryStoreEvictionPolicy=\"");
        ehcacheConfig.append(context.getSingleValueParameter(ecacheConfigurationsXPath + "/defaultCache/@memoryStoreEvictionPolicy"));
        ehcacheConfig.append("\"/>\n");
        ehcacheConfig.append("</ehcache>");

        return ehcacheConfig.toString().getBytes(context.getDefaultEncoding());
    }
    
    /**
     * The logger for this class
     */
    private Logger log = null;
    
    /**
     * The ehcache cache manager singleton.
     */
    private static CacheManager ehCacheManager = null;

    /**
     * Max size of a cacheable resource (0 == unlimited)
     */
    private static int maxCacheableResourceSize = 0;

    /**
     * The ehcache store for pages requested by the context
     */
    private Cache theCache = null;

    /**
     * The attribute name that will be stored into the resource
     * on every cache-hit.
     */
    private static final String CACHE_HIT_ATTRIBUTE = "Cache-Hit";

    /**
     * an empty string..
     */
    private static final String EMPTY_STRING = "";
    /**
     * An header for the cache control
     */
    private static final String PRAGMA_HEADER = "Pragma";
    /**
     * No cache value for the Pragma header
     */
    public final static String PRAGMA_NO_CACHE = "no-cache";
    /**
     * Another Header for the cache control
     */
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    /**
     * Cache control value that means: ok, cache it!
     */
    public final static String CACHE_CONTROL_PUBLIC = "public";
    /**
     * Cache control value that means: You can cache it only for this user.
     */
    public final static String CACHE_CONTROL_PRIVATE = "private";
    /**
     * No cache value for the Pragma header
     */
    public final static String CACHE_CONTROL_NO_CACHE = "no-cache";
    /**
     * Cache control value that means: no, don't cache this
     */
    public final static String CACHE_CONTROL_NO_STORE = "no-store";
    /**
     * Cache control value that means: this can be cached for X seconds
     */
    public final static String CACHE_CONTROL_MAX_AGE = "max-age=";
    /**
     * Similar to the previous but for shared caches
     */
    public final static String CACHE_CONTROL_S_MAXAGE = "s-maxage=";
    /**
     * The name of this plugin.
     */
    private static final String name = "Ehcache Cache Provider";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This is a wrapper for the famouns Ehcache subsystem.<br/>"
            + "Namely, this plugin uses the Ecache engine to implement a robust and fast "
            + "cache backend for the reverse proxy operations.<br/>"
            + "It have a side effect in terms of RAM occupation and disk activity, but it can give "
            + "a great speed up to the proxy operations.";
}

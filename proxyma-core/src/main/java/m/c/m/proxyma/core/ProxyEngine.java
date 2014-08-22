package m.c.m.proxyma.core;

import m.c.m.proxyma.plugins.caches.CacheProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;

/**
 * <p>
 * This class is the main skeleton of this whole project and is a mixture of many design patterns.
 * </p><p>
 * <ul>
 * <li>It's like a "Builder" because it separates the building of a complex object (the proxy response) from its implementation.</li>
 * <li>It's like a "Mediator" because it encapsulate the collaboration strategies of a group of objects that doesn't know anything about each other</li>
 * <li>It's like a "Strategy" because the algorithms to build of the proxy response are choosen at run time form the ProxyFolder configuration</li>
 * <li>It's like a "Template Method" because it defines the skeleton of the algorithm and delegates to the plugins the implementation of the manipulation algorithms</li>
 * </ul>
 * </p><p>
 * Its purpose is to manage a ProximaResource (that has a request inside) in order to fill the response with the requested data using
 * the provided plugins. <br/>
 * IMPORTANT: The engine guarantees that the preprocessors and the transformer are executed respecting the "executiPriority" defined
 * into the proxyma-config.xml. Plugins with a lower priority are execute first and plugins with an higher priority are executed lastly
 * (BTW: plugins with the same priority are executed in random order).
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxyEngine.java 179 2010-07-03 11:49:04Z marcolinuz $
 */
public class ProxyEngine {

    /**
     * The constructor for this class.
     *
     * @param context the context to use to retrive configuration data
     */
    public ProxyEngine(ProxymaContext context) {
        //initialize the logger for this class.
        log = context.getLogger();
    }

    /**
     * This is the core method of this project.
     * It applies to the passed resource all the configured plugins defined
     * into the ProxyFolder that is responsible of its creation.
     *
     * @param aResource a resource to masquerade by the proxy.
     */
    public int doProxy(ProxymaResource aResource) throws IOException, Exception {
        //A new resource request has come..
        int retValue = STATUS_OK;
        ProxymaContext context = aResource.getContext();
        ProxymaRequest request = aResource.getRequest();
        ResourceHandler defaultSerializer = availableSerializers.get(ProxymaTags.UNSPECIFIED_SERIALIZER);

        //set proxyma root URI
        aResource.setProxymaRootURI(getProxymaRootURI(request));

        // *** Try to understand what kind of request was come and if it belongs to any proxyFolder ***
        String subPath = request.getRequestURI().replace(request.getContextURLPath(), EMPTY_STRING);
        ProxymaResponseDataBean responseData = null;
        if (subPath == null || EMPTY_STRING.equals(subPath)) {
            //The path is not complete, redirect the client to proxyma root path
            try {
                //prepare a redirect response to the "Proxyma root uri"
                log.fine("Requested the proxyma path without trailing \"/\".. Redirecting to root uri: " + aResource.getProxymaRootURLAsString());
                responseData = ProxyInternalResponsesFactory.createRedirectResponse(aResource.getProxymaRootURLAsString()+"/", context);
            } catch (MalformedURLException ex) {
                //if the URL is malformed send back an error page.
                log.severe("Malformed URL found (" + aResource.getProxymaRootURLAsString() + ") for the proxyma root URI!");
                responseData = ProxyInternalResponsesFactory.createErrorResponse(STATUS_BAD_REQUEST, context);
            }

            //Serialize the response with the default serializer
            aResource.getResponse().setResponseData(responseData);
            retValue = responseData.getStatus();
            defaultSerializer.process(aResource);
        } else if (PATH_SEPARATOR.equals(subPath)) {
            //The Proxyma root path was requested.
            if (isEnableShowFoldersListOnRootURI()) {
                //The folders list page is enabled, prepre the response with the folders list
                log.fine("Requested the \"registered folders page\", generating it..");
                responseData = ProxyInternalResponsesFactory.createFoldersListResponse(context);
            } else {
                //The folders list page is disabled by configuration, send a 404 error response
                log.fine("Requested the proxyma root uri but the \"registered folders page\" is denyed by configuration.");
                responseData = ProxyInternalResponsesFactory.createErrorResponse(STATUS_NOT_FOUND, context);
            }
            //Serialize the response with the default serializer
            aResource.getResponse().setResponseData(responseData);
            retValue = responseData.getStatus();
            defaultSerializer.process(aResource);
        } else {
            //Searching into the context for a matching proxyFolder using "URLEncoded value"
            String URLEncodedProxyFolder = subPath.split("/")[1];
            ProxyFolderBean folder = context.getProxyFolderByURLEncodedName(URLEncodedProxyFolder);

            if (folder == null) {
                //The proxyFolder doesn't exists, (send a 404 error response)
                log.fine("Requested an unexistent or proxy folder (" + URLEncodedProxyFolder + "), sending error response..");
                responseData = ProxyInternalResponsesFactory.createErrorResponse(STATUS_NOT_FOUND, context);
                aResource.getResponse().setResponseData(responseData);
                retValue = responseData.getStatus();
                defaultSerializer.process(aResource);
            } else if (!folder.isEnabled()) {
                //The requested proxy folder exixts but is disabled (send a 403 error response)
                log.fine("Requested a disabled or proxy folder (" + folder.getFolderName() + "), sending error response..");
                responseData = ProxyInternalResponsesFactory.createErrorResponse(STATUS_FORBIDDEN, context);
                aResource.getResponse().setResponseData(responseData);
                retValue = responseData.getStatus();
                defaultSerializer.process(aResource);
            } else {
                //The requested proxy folder exists and is enabled.
                log.fine("Requested an available and enabled proxy folder content.. go to process it!");
                
                //Set the matched proxyFolder into the resource
                aResource.setProxyFolder(folder);
                log.finer("Destination URL: " + folder.getDestinationAsString());

                //Set the destination subpath into the resource
                aResource.setDestinationSubPath(subPath.replaceFirst(PATH_SEPARATOR+URLEncodedProxyFolder, EMPTY_STRING));
                
                if (aResource.getDestinationSubPath().contains("ext-grid.js")) {
                	System.out.println("ext-grid.js");
                }

                // *** NOW I know what the user has Just asked for ***
                Iterator<String> configuredPlugins = null;
                ResourceHandler plugin = null;
                CacheProvider cache = null;
                try {
                    //Applying all the folder-specific preprocessors to the resource in registration order
                    configuredPlugins = folder.getPreprocessors().iterator();
                    while (configuredPlugins.hasNext()) {
                        plugin = availablePreprocessors.get(configuredPlugins.next());
                        log.finer("Applying preprocessor: " + plugin.getName());
                        plugin.process(aResource);
                    }

                    //Use the folder-specific cache provider to search for the wanted resource into the cache subsystem
                    cache = availableCacheProviders.get(folder.getCacheProvider());
                    if (!cache.getResponseData(aResource)) {
                        log.fine("Resource not found into cache provider: " + cache.getName());
                        // *** The resource is not present into the cache **
                        //Go to retrive it using the folder-specific retriver
                        plugin = availableRetrivers.get(folder.getRetriver());
                        log.finer("Getting resource with the "+ plugin.getName());
                        plugin.process(aResource);

                        //Set the Server Header into the response:
                        aResource.getResponse().getResponseData().deleteHeader(SERVER_HEADER);
                        aResource.getResponse().getResponseData().addHeader(SERVER_HEADER, context.getProxymaVersion());

                        //Apply the folder-specific transformer in registration order
                        configuredPlugins = folder.getTransformers().iterator();
                        while (configuredPlugins.hasNext()) {
                            plugin = availableTransformers.get(configuredPlugins.next());
                            log.finer("Applying transformer: " + plugin.getName());
                            plugin.process(aResource);
                        }

                        //Invoke the cache plugin to store the resource if it's cacheable
                        cache.storeResponseDataIfCacheable(aResource);
                    }

                    //Finally pass the resource to the folder-specific serializer
                    plugin = availableSerializers.get(folder.getSerializer());
                    log.finer("Serializing the resource with the " + plugin.getName());
                    retValue = aResource.getResponse().getResponseData().getStatus();
                    plugin.process(aResource);

                } catch (Exception e) {
                    //If any unexpected exception is thrown, send the back the error resource to the client.
                    log.warning("There was an error Processing the request \"" + aResource.getRequest().getRequestURI() +  "\" by the Proxy folder \"" + folder.getFolderName() + "\"");
                    e.printStackTrace();
                    responseData = ProxyInternalResponsesFactory.createErrorResponse(STATUS_INTERNAL_SERVER_ERROR, context);
                    aResource.getResponse().setResponseData(responseData);
                    retValue = responseData.getStatus();
                    defaultSerializer.process(aResource);
                }
            }
        }

        //Return the status to the caller
        return retValue;
    }

    /**
     * Get a Collection of CacheProviders registered into the Engine.
     *
     * @return the mentioned collection.
     */
    public Collection<CacheProvider> getRegisteredCachePlugins () {
        return availableCacheProviders.values();
    }

    /**
     * Get a collection of the registered plugins by type.
     *
     * @param type the type of the plugins that have to be returned
     * @return The collection of registered resource handlers
     */
    public Collection<ResourceHandler> getRegisteredPluginsByType(ProxymaTags.HandlerType type) {
        Collection <ResourceHandler> retValue = null;
        switch (type) {
                case PREPROCESSOR:
                    retValue = availablePreprocessors.values();
                    break;

                case RETRIVER:
                    retValue = availableRetrivers.values();
                    break;

                case TRANSFORMER:
                    retValue = availableTransformers.values();
                    break;

                case SERIALIZER:
                    retValue = availableSerializers.values();
                    break;

                default:
                    log.warning("What kind of ResourceHandler type is \"" + type + "\"?!?..");
                    retValue = new LinkedList();
                    break;
            }

        return retValue;
    }

    /**
     * Protected method to set the available cache providers.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble cache providers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableCacheProviders(HashMap<String, CacheProvider> availableCacheProviders) {
        this.availableCacheProviders = availableCacheProviders;
    }

    /**
     * Protected method to set the available preprocessor plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble preprocessors.
     * @see ProxyEngineFactory
     */
    protected void setAvailablePreprocessors(HashMap<String, ResourceHandler> availablePreprocessors) {
        this.availablePreprocessors = availablePreprocessors;
    }

    /**
     * Protected method to set the available retriver plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble retrivers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableRetrivers(HashMap<String, ResourceHandler> availableRetrivers) {
        this.availableRetrivers = availableRetrivers;
    }

    /**
     * Protected method to set the available serializer plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble serializers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableSerializers(HashMap<String, ResourceHandler> availableSerializers) {
        this.availableSerializers = availableSerializers;
    }

    /**
     * Protected method to set the available transformer plugins.<br/>
     * It's used by the ProxyEngineFactory to create an instance of this class.
     * @param availableCacheProviders the new Map of availeble transformers.
     * @see ProxyEngineFactory
     */
    protected void setAvailableTransformers(HashMap<String, ResourceHandler> availableTransformers) {
        this.availableTransformers = availableTransformers;
    }

    /**
     * Get the new value for the flag<br/>
     * If true, proxyma will show the list of the registered folders
     * if the client access to the root uri of the proxy.
     * @return true or false
     */
    protected boolean isEnableShowFoldersListOnRootURI() {
        return enableShowFoldersListOnRootURI;
    }

    /**
     * Sets  the new value for the flag.
     * @param enableShowFoldersListOnRootURI
     * @see isShowFoldersListOnRootURI
     */
    protected void setEnableShowFoldersListOnRootURI(boolean showFoldersListOnRootURI) {
        this.enableShowFoldersListOnRootURI = showFoldersListOnRootURI;
    }


    /**
     * Calculates the root URI of the reverse proxy context.
     *
     * @param request the request to use for the calculus.
     * @return http://proxyma.host[:proxymaPort]/
     */
    private String getProxymaRootURI (ProxymaRequest request) {
        StringBuffer retVal = new StringBuffer();

        retVal.append(request.getScheme()).append("://");
        retVal.append(request.getServerName());

        if ((request.getServerPort() == 80) && (HTTP_SCHEME.equals(request.getScheme()))) {
        } else if ((request.getServerPort() == 443) && (HTTPS_SCHEME.equals(request.getScheme()))) {
        } else {
            retVal.append(":").append(request.getServerPort());
        }
        retVal.append(request.getContextURLPath());
        
        return retVal.toString();
    }

    /**
     * The collection of all available cache providers plugins
     */
    private HashMap<String, CacheProvider> availableCacheProviders = null;

    /**
     * The collection of all available preprocessor plugins
     */
    private HashMap<String, ResourceHandler> availablePreprocessors = null;

    /**
     * The collection of all available retriver plugins
     */
    private HashMap<String, ResourceHandler> availableRetrivers = null;

    /**
     * The collection of all available transformer plugins
     */
    private HashMap<String, ResourceHandler> availableTransformers = null;

    /**
     * The collection of all available serializer plugins
     */
    private HashMap<String, ResourceHandler> availableSerializers = null;

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * flag that tells to proxyma to show the list of the registered folders
     * if the client access to the root uri of the proxy.
     */
    private boolean enableShowFoldersListOnRootURI = false;

    /* SOME USEFUL CONSTANTS */
    /**
     * an empty string..
     */
    private static final String EMPTY_STRING = "";

    /**
     * The separator character for the URI paths
     */
    private static final String PATH_SEPARATOR = "/";

    /**
     * http
     */
    private static final String HTTP_SCHEME = "http";

    /**
     * https
     */
    private static final String HTTPS_SCHEME = "https";

    /**
     * The originating server header name
     */
    private static final String SERVER_HEADER = "Server";
    
    /**
     * Http status code for "not found" resources
     */
    private static final int STATUS_NOT_FOUND = 404;

    /**
     * Http status code for "forbidden" resources
     */
    private static final int STATUS_FORBIDDEN = 403;

    /**
     * Http status code for "Malformed requests"
     */
    private static final int STATUS_BAD_REQUEST = 400;

    /**
     * Http status code for "Internal Server Error"
     */
    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    /**
     * Http status code for "Ok"
     */
    private static final int STATUS_OK = 200;
}

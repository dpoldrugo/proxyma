package m.c.m.proxyma.core;

import m.c.m.proxyma.plugins.caches.CacheProvider;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;

/**
 * <p>
 * This class is the factory method of the Proxy Engine
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxyEngineFactory.java 159 2010-06-27 20:41:25Z marcolinuz $
 */
public class ProxyEngineFactory {
    /**
     * The constructor for this class.
     *
     * @param context the context to use to retrive configuration data
     */
    public ProxyEngine createNewProxyEngine(ProxymaContext context) throws IllegalAccessException {
        //set the logger if is not already set.
        if (log == null)
            this.log = context.getLogger();

        //initialize the logger for this class.
        ProxyEngine newEngine = new ProxyEngine(context);

        //Create the Maps for the available plugins
        HashMap<String, CacheProvider> availableCacheProviders = new HashMap();
        HashMap<String, ResourceHandler> availablePreprocessors = new HashMap();
        HashMap<String, ResourceHandler> availableRetrivers = new HashMap();
        HashMap<String, ResourceHandler> availableTransformers = new HashMap();
        HashMap<String, ResourceHandler> availableSerializers = new HashMap();

        // *** Load the available Cache Provider Plugins ***
        Iterator<String> availableCaches = context.getMultiValueParameter(ProxymaTags.AVAILABLE_CACHE_PROVIDERS+"/@class").iterator();
        loadCacheProviders(availableCaches, availableCacheProviders, context);
        
        // *** Load the available Resource Handler Plugins ***
        Iterator<String> availablePlugins = null;

        //Load the preprocessors
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_PREPROCESSORS+"/@class").iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.PREPROCESSOR, availablePreprocessors, context);

        //Load the retrivers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_RETRIVERS+"/@class").iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.RETRIVER, availableRetrivers, context);

        //Load the transformers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_TRANSFORMERS+"/@class").iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.TRANSFORMER, availableTransformers, context);

        //Load the serializers
        availablePlugins = context.getMultiValueParameter(ProxymaTags.AVAILABLE_SERIALIZERS+"/@class").iterator();
        loadPlugins(availablePlugins, ProxymaTags.HandlerType.SERIALIZER, availableSerializers, context);

        //set the values into the proxy engine
        newEngine.setAvailableCacheProviders(availableCacheProviders);
        newEngine.setAvailablePreprocessors(availablePreprocessors);
        newEngine.setAvailableRetrivers(availableRetrivers);
        newEngine.setAvailableSerializers(availableSerializers);
        newEngine.setAvailableTransformers(availableTransformers);

        //Set the value for the "show folders on root uri" flag
        String configValue = context.getSingleValueParameter(ProxymaTags.GLOBAL_SHOW_FOLDERS_LIST);
        if (configValue != null)
            newEngine.setEnableShowFoldersListOnRootURI(configValue.equalsIgnoreCase("true")?true:false);

        //return the builded object
        return newEngine;
    }

    /**
     * Load into the cache provider container the passed list of plugins.
     * NOTE: Before load the plugin it checks if its type is correct.
     *       if not, the plugin will not be loaded.
     *
     * @param availablePlugins an iterator of plugin class names to load
     * @param container the cache provider container to fill.
     */
    private void loadCacheProviders(Iterator<String> availableCaches, HashMap<String, CacheProvider> availableCacheProviders, ProxymaContext context) {
        String cachePluginName = "";
        while (availableCaches.hasNext()) {
            try {
                cachePluginName = availableCaches.next();
                Class contextClass = Class.forName("m.c.m.proxyma.context.ProxymaContext");
                Class cachePluginClass = Class.forName(cachePluginName);
                Constructor pluginConstructor = cachePluginClass.getConstructor(contextClass);
                Object cachePlugin = pluginConstructor.newInstance(context);
                if (cachePlugin instanceof CacheProvider) {
                    registerCacheProvider((CacheProvider)cachePlugin, availableCacheProviders);
                } else {
                    log.warning("The Class \"" + cachePluginName  + "\" is not a CacheProvider.. plugin not loaded.");
                }
            } catch (ClassNotFoundException ex) {
                log.log(Level.WARNING, "Cache Provider \"" + cachePluginName  + "\" not found.. plugin not loaded.", ex);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Cache Provider \"" + cachePluginName  + "\" cannot be instantiated.. plugin not loaded.", ex);
            }
        }
    }

    /**
     * Load into the proper plugin containers the passed list of plugins.
     *
     * @param availablePlugins an iterator of plugin class names to load
     */
    private void loadPlugins(Iterator<String> availablePlugins, ProxymaTags.HandlerType requiredType, HashMap<String, ResourceHandler> pluginContainer, ProxymaContext context) {
        String pluginName = "";
        while (availablePlugins.hasNext()) {
            try {
                pluginName = availablePlugins.next();
                Class contextClass = Class.forName("m.c.m.proxyma.context.ProxymaContext");
                Class pluginClass = Class.forName(pluginName);
                Constructor pluginConstructor = pluginClass.getConstructor(contextClass);
                Object thePlugin = pluginConstructor.newInstance(context);
                if ((thePlugin instanceof ResourceHandler) && ((ResourceHandler)thePlugin).getType() == requiredType) {
                    registerNewPlugin((ResourceHandler)thePlugin, pluginContainer);
                } else {
                    log.warning("The Class \"" + pluginName  + "\" is not a " + requiredType + ".. plugin not loaded.");
                } 
            } catch (ClassNotFoundException ex) {
                log.log(Level.WARNING, "Plugin \"" + pluginName  + "\" not found, plugin not loaded.", ex);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Plugin \"" + pluginName  + "\" cannot be instantiated, plugin not loaded.", ex);
            }
        }
    }

    /**
     * Register a new cache provoder implementation into the cache collection
     * @param providerImpl class that implements a CacheProvider
     */
    private void registerCacheProvider(CacheProvider providerImpl, HashMap<String, CacheProvider> availableCacheProviders) {
        if (providerImpl == null) {
            log.warning("Null provider implementation parameter.. Ignoring operation");
        } else {
            boolean exists = availableCacheProviders.containsKey(providerImpl.getClass().getName());
            if (exists) {
                log.warning("Cache provider \"" + providerImpl.getClass().getName() + "\" already registered.. nothing done.");
            } else {
                log.finer("Adding cache provider " + providerImpl.getClass().getName());
                availableCacheProviders.put(providerImpl.getClass().getName(), providerImpl);
            }
        }
    }

    /**
     * Register a new resource handler implementation into the proper collection
     *
     * @param handlerImpl class that implements the handler
     */
    private void registerNewPlugin(ResourceHandler pluginImpl, HashMap<String, ResourceHandler> pluginContainer) {
        if (pluginImpl == null) {
            log.warning("Null handler implementation parameter.. Ignoring operation");
        } else {
            boolean exists;
            switch (pluginImpl.getType()) {
                case PREPROCESSOR:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Preprocessor \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finer("Adding preprocessor " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case RETRIVER:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Retriver \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finer("Adding retriver " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case TRANSFORMER:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Transformer \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finer("Adding transformer " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                case SERIALIZER:
                    exists = pluginContainer.containsKey(pluginImpl.getClass().getName());
                    if (exists) {
                        log.warning("Serializer \"" + pluginImpl.getClass().getName() + "\" already registered.. nothing done.");
                    } else {
                        log.finer("Adding serializer " + pluginImpl.getClass().getName());
                        pluginContainer.put(pluginImpl.getClass().getName(), pluginImpl);
                    }
                    break;

                default:
                    log.warning("Unknown ResourceHandler type \"" + pluginImpl.getType() + "\" .. nothing done.");
                    break;
            }
        }
    }

    /**
     * The logger of the context
     */
    private Logger log = null;
}

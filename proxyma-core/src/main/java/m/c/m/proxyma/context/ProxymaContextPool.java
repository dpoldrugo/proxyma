package m.c.m.proxyma.context;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This is Singleton class that stores an hashtable of ProxyContexts.
 * Every record in the hashtable is referred to a single instance of Proxyma and
 * countains the configurations for all the ProxyFolders for that instance.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaContextPool.java 163 2010-06-28 23:03:13Z marcolinuz $
 */
public class ProxymaContextPool {

    /**
     * Constructor for this class
     */
    public ProxymaContextPool() {
        this.proxymaContexts = new ConcurrentHashMap();
        log = Logger.getLogger("");
    }

    /**
     * Static method to call to obtain the singleton class that manages
     * the contexts for all the proxyma instances in this virtual machine.
     *
     * @return the only instance of the global RuleSetsPool
     */
    public static synchronized ProxymaContextPool getInstance() {
        if (instance == null) {
            instance = new ProxymaContextPool();
            log.info("First run: creating the ProxymaContextPool Singleton instance.");
        }
        return instance;
    }

    /**
     * Creates and register a new context into the pool.
     * if the context name already exists it returns the existing one without
     * create it.
     *
     * @param contextName a unique string that identifies the context
     * (use "default" if you don't understand what this means)
     * @param contextBaseURI The base URI to reach this context
     * @param configurationFile The configuration file for the context
     *
     * @return the created context or the existing context with the same name
     */
    public ProxymaContext registerNewContext(String contextName, String contextBaseURI, String configurationFile, String logsDirectory) {
        ProxymaContext theNewContext = null;
        if (proxymaContexts.containsKey(contextName)) {
            log.info("Context \"" + contextName + "\" already exists, returning it..");
            theNewContext = getContextByName(contextName);
        } else {
            log.finer("Creatibg new context \"" + contextName + "\"");
            theNewContext = new ProxymaContext(contextName, contextBaseURI, configurationFile, logsDirectory);
            proxymaContexts.put(contextName, theNewContext);
        }
        return theNewContext;
    }

    /**
     * Unregister an existing and empty context from the pool
     * NOTE: This method requires that the context is empty.
     * In other words, it will remove the context only if it doesn't contain
     * any ProxyFolderBean.
     *
     * @param the context to unregister
     * @throws IllegalArgumentException if the doesn't exists
     * @throws NullArgumentException if a null argument is passed to this method
     * @throws IllegalStateException if the context is not empty
     */
    public void unregisterContext(ProxymaContext context) throws NullArgumentException, IllegalArgumentException, IllegalStateException {
        if (context == null) {
            log.warning("Null context argument received.. operation aborted");
            throw new NullArgumentException("Null context argument received.. operation aborted");
        } else if (context.getProxyFoldersCount() > 0) {
            log.warning("Context \"" + context.getName() + "\" is not empty.. operation aborted");
            throw new IllegalStateException("Context \"" + context.getName() + "\" is not empty.. operation aborted");
        } else if (!proxymaContexts.containsKey(context.getName())) {
            log.warning("Context \"" + context.getName() + "\" doesn't exists.. operation aborted.");
            throw new IllegalArgumentException("Context \"" + context.getName() + "\" doesn't exists.. operation aborted.");
        } else {  
            log.finer("Removing context \"" + context.getName() + "\" from the pool.");
            proxymaContexts.remove(context.getName());
        }
    }

    /**
     * Obtain an existing context by name.
     *
     * @param contextName a unique string that identifies the wanted context
     * (use "default" if you don't understand what this means)
     *
     * @return a ProxymaContext that countains the ProxyFolders configured
     * for the requested context.
     */
    public ProxymaContext getContextByName(String contextName) {
        ProxymaContext retValue = null;
        if (proxymaContexts.containsKey(contextName)) {
            log.finer("Context \"" + contextName + "\" found");
            retValue = (ProxymaContext) proxymaContexts.get(contextName);
        } else {
            log.warning("Context \"" + contextName + "\" does not exixts.");
        }
        return retValue;
    }

    /**
     * Returns the name of the registered contexts.
     * @return the registeres context names
     */
    public Enumeration<String> getRegisteredContextNames() {
        return proxymaContexts.keys();
    }

    /**
     * The contexts container.
     */
    private ConcurrentHashMap<String, ProxymaContext> proxymaContexts = null;
    /**
     * The single instance available for this class.
     */
    private static ProxymaContextPool instance = null;
    /**
     * The logger for this class
     */
    private static Logger log = null;
}

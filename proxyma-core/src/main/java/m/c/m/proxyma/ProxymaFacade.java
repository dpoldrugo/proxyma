package m.c.m.proxyma;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.context.ProxymaContextPool;
import m.c.m.proxyma.context.ProxyFolderFactory;
import m.c.m.proxyma.core.ProxyEngine;
import m.c.m.proxyma.core.ProxyEngineFactory;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResourceFactory;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class is the main interface (Facade) to intercact with Proxyma.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaFacade.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxymaFacade {
    /**
     * the default constructor for this class.
     */
    public ProxymaFacade () {
        this.foldersFactory = new ProxyFolderFactory();
        this.resourceFactory = new ProxymaResourceFactory();
        this.proxyFactory = new ProxyEngineFactory();
        init();
    }

    /**
     * Register a new Proxyma Context into the Application Environment.
     * You need a context to make any operation with proxyma.
     *
     * @param contextName The unique name for the context to create. You will use it from now to operate with the context.
     * @param contextBaseURI The base URI for this context. In other words, the URI to prepend to the "ProxyFolders".
     * @param configFilePath An xml configuration file for the context.
     * @return a newly generated ProxymaContext
     * @see ProxymaContext
     * @throws IllegalArgumentException if the context already exists
     */
    public ProxymaContext createNewContext (String contextName, String contextBaseURI, String configFilePath, String logsDirectoryPath) throws IllegalArgumentException {
        ProxymaContext retValue = null;
        ProxymaContextPool pool = ProxymaContextPool.getInstance();
        retValue = pool.registerNewContext(contextName, contextBaseURI, configFilePath, logsDirectoryPath);
        return retValue;
    }

    /**
     * Remove an existin  Proxyma Context form the Application Environment.
     * NOTE: This method requires that the context is empty.
     * In other words, The context is removed only if it doesn't contain
     * any ProxyFolderBean into itself.
     *
     * @param the context to unregister
     * @throws IllegalArgumentException if the doesn't exists
     * @throws NullArgumentException if a null argument is passed to this method
     * @throws IllegalStateException if the context is not empty
     */
    public void destroyContext (ProxymaContext context) throws NullArgumentException, IllegalArgumentException, IllegalStateException {
        ProxymaContextPool pool = ProxymaContextPool.getInstance();
        pool.unregisterContext(context);
    }


    /**
     * Obtain an existing context using its unique name.
     *
     * @param contextName the name of the context to retrive.
     * @return the requested context (or Null if the context doesn't exists)
     */
    public ProxymaContext getContextByName (String contextName) {
        ProxymaContext retValue = null;
        ProxymaContextPool pool = ProxymaContextPool.getInstance();
        retValue = pool.getContextByName(contextName);
        return retValue;
    }

    /**
     * Returns an Enumeration of all the registered context names
     * @return the registered context names
     */
    public Enumeration<String> getRegisteredContextNames() {
        ProxymaContextPool pool = ProxymaContextPool.getInstance();
        return pool.getRegisteredContextNames();
    }


    /**
     * Builds a new default ProxyFolder to the specified destination setting.<br/>
     * The proxy folder will be disabled and attached to the provided context.
     *
     * @param FolderName the path (and name) of the proxy folder.
     * @param destination the destination URI to masquerade
     * @param context the proxyma context where to take default settings.
     * @throws NullArgumentException if some parameter is null
     * @throws IllegalArgumentException if the folder name or the destination parameter are invalid or malformed
     * @throws UnsupportedEncodingException if the default encoding charset specified on the configuration is not supported.
     */
    public ProxyFolderBean createNewProxyFolder (String FolderName, String destination, ProxymaContext context) 
            throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        ProxyFolderBean theNewContext = foldersFactory.createNewProxyFolder(FolderName, destination, context);
        context.addProxyFolder(theNewContext);
       return theNewContext;
    }

    /**
     * Get from the passed context an existing proxyFolder searching it by its URL encoded name
     *
     * @param FolderName the folder to retrive
     * @param context the context to inspect
     * @return the requested ProxyFolderBean (or null if it's not found)
     */
    public ProxyFolderBean getProxyFolderByURLEncodedName(String urlEncodedName, ProxymaContext context) {
        return context.getProxyFolderByURLEncodedName(urlEncodedName);
    }

    /**
     * Remove from the passed context an existing proxyFolder
     *
     * @param theFolder the proxy folder to remove
     * @param context the context to inspect
     * @throws IllegalArgumentException if the context doesn't exist
     * @throws NullArgumentException if the argument is null
     */
    public void removeProxyFolder (ProxyFolderBean theFolder, ProxymaContext context) throws IllegalArgumentException, NullArgumentException {
        context.removeProxyFolder(theFolder);
    }

    /**
     * Enable the passed proxyFolder
     *
     * @param theFolder the proxy folder to remove
     * @param context the context to inspect
     * @throws IllegalArgumentException if the context doesn't exist
     * @throws NullArgumentException if the argument is null
     */
    public void enableProxyFolder (ProxyFolderBean theFolder) throws NullArgumentException {
        if (theFolder == null)
            throw new NullArgumentException("Can't enable a null Proxy-Folder");
        theFolder.setEnabled(true);
    }

    /**
     * Diable the passed proxyFolder
     *
     * @param theFolder the proxy folder to remove
     * @param context the context to inspect
     * @throws IllegalArgumentException if the context doesn't exist
     * @throws NullArgumentException if the argument is null
     */
    public void disableProxyFolder (ProxyFolderBean theFolder) throws NullArgumentException {
        if (theFolder == null)
            throw new NullArgumentException("Can't disable a null Proxy-Folder");
        theFolder.setEnabled(false);
    }

    /**
     * Returns all the proxyFolders form the passed context.
     * 
     * @param context the context to inspect for folders
     * @return a Collection of proxy folders 
     */
    public Collection<ProxyFolderBean> getContextProxyFolders (ProxymaContext context) {
        return context.getProxyFoldersAsCollection();
    }

    /**
     * This method creates a complete instance of ProxymaResource form a
     * servlet request, a servlet response and a proxyma context.
     * The produced resource can be handled by the reverse proxy engine and
     * by any of its registered plugins.
     *
     * @param request the servlet container request.
     * @param response the servlet container response
     * @param context the proxyma context where the resource will live.
     * @return an resource that can be directly handled by the ProxyEngine.
     * @throws NullArgumentException if any of the passed parameters is null
     */
     public ProxymaResource createNewResource(HttpServletRequest request, HttpServletResponse response, ProxymaContext context)
        throws NullArgumentException {
        return resourceFactory.createNewResource(request, response, context);
     }

     /**
      * This method creates a new instance of the Proxyma Proxy-Engine, the core of this project.
      *
      * @param context the context where the proxy will work
      * @return a new instance of proxy
      * @throws IllegalAccessException if there are some troubles with the plugins loading.
      */
     public ProxyEngine createNewProxyEngine (ProxymaContext context) throws IllegalAccessException {
        return proxyFactory.createNewProxyEngine(context);
     }

    /**
     * This method initialize the proxyma subsystems.
     * It is fired any time a new instance of Facade is created but it has
     * effect only the first time.
     */
    private synchronized void init () {
        if (!initialized) {
            log = Logger.getLogger("");
            log.info("Initializing Proxyma subsystems..");

            // Initialize the contexts pool
            ProxymaContextPool instance = ProxymaContextPool.getInstance();

            initialized = true;
        } else {
            log.info("Proxyma subsystems already initialized..");
        }
    }


    /**
     * The ProxyFolder Factory used by this instance
     */
    private ProxyFolderFactory foldersFactory = null;

    /**
     * The resource factory used by this instance
     */
    private ProxymaResourceFactory resourceFactory = null;

    /**
     * The proxy engine factory used by this instance
     */
    private ProxyEngineFactory proxyFactory = null;

    /**
     * The logger for this class
     */
    private static Logger log = null;

    /**
     * It is true if the "init" method as alrady run
     */
    private static boolean initialized = false;
}

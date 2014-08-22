package m.c.m.proxyma.context;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.log.ProxymaLoggersUtil;
import m.c.m.proxyma.rewrite.URLUtils;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class is the main context for an instance of Proxyma.
 * Multiple instance of proxyma are allowed to run into a single VM.
 * It countains the logic to access and get parameters form a required
 * configuration logFile.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaContext.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class ProxymaContext {
    /**
     * Default constructor for this Class
     *
     * @param contextName the name of the context to create
     * @param contextBaseURI Base URI of the context
     * @param configurationFile proxyma configuration file to load
     * @param logsDirectoryPath directory where to write all the logs
     */
    public ProxymaContext (String contextName, String contextBaseURI, String configurationFile, String logsDirectoryPath) {
        // Initialize private attributes
        try {
            this.contextName = contextName;
            this.proxymaContextBasePath = contextBaseURI;
            this.logsDirectoryPath = logsDirectoryPath;
            proxyFoldersByURLEncodedName = new ConcurrentHashMap<String, ProxyFolderBean>();
            proxyFoldersByDestinationHost = new ConcurrentHashMap<String, LinkedList<ProxyFolderBean>>();
            config = new XMLConfiguration(configurationFile);
            config.setExpressionEngine(new XPathExpressionEngine());
            if (this.log == null) {
                //create a unique logger for the whole context
                String name = ProxymaTags.DEFAULT_LOGGER_PREFIX + "." + contextName;
                this.log = Logger.getLogger(name);

                String logFile = logsDirectoryPath + "proxyma-" + contextName + ".log";
                String level = getSingleValueParameter(ProxymaTags.GLOBAL_LOGLEVEL);
                int maxSize = Integer.parseInt(getSingleValueParameter(ProxymaTags.GLOBAL_LOGFILE_MAXSIZE));
                int retention = Integer.parseInt(getSingleValueParameter(ProxymaTags.GLOBAL_LOGFILES_RETENTION));
                ProxymaLoggersUtil.initializeContextLogger(this.log, logFile, level, maxSize, retention);
            }
            this.defaultEncoding = getSingleValueParameter(ProxymaTags.GLOBAL_DEFAULT_ENCODING);
            this.proxymaVersion = "Proxyma-NG (Rel. " + getSingleValueParameter(ProxymaTags.CONFIG_FILE_VERSION) + ")";
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get a proxy folder by folder name (if exists)
     *
     * @param proxyFolderURLEncodedName the wanted ProxyFolderBean
     * @return null if the proxyFolder doesn't exists.
     */
    public ProxyFolderBean getProxyFolderByURLEncodedName(String proxyFolderURLEncodedName) {
       ProxyFolderBean retVal = null;
       log.finer("Searching for Proxy folder " + proxyFolderURLEncodedName);
       if (proxyFolderURLEncodedName == null) {
           log.warning("Null proxyFolderName parameter.. Ignoring operation");
       } else if (proxyFoldersByURLEncodedName.containsKey(proxyFolderURLEncodedName)) {
           retVal = proxyFoldersByURLEncodedName.get(proxyFolderURLEncodedName);
       } else {
           log.finer("Proxy folder " + proxyFolderURLEncodedName + " not found.");
       }
       return retVal;
    }

    /**
     * Get a proxy folder by folder name (if exists)
     *
     * @param proxyFolderURLEncodedName the wanted ProxyFolderBean
     * @return null if the proxyFolder doesn't exists.
     */
    public Collection<ProxyFolderBean> getProxyFolderByDestinationHost(String proxyFolderDestinationHost) {
       LinkedList retVal = null;
       log.finer("Searching for Proxy folder destination host " + proxyFolderDestinationHost);
       if (proxyFolderDestinationHost == null) {
           log.warning("Null proxyFolderDestination parameter.. Ignoring operation");
       } else if (proxyFoldersByDestinationHost.containsKey(proxyFolderDestinationHost)) {
           retVal = proxyFoldersByDestinationHost.get(proxyFolderDestinationHost);
       } else {
           log.finer("Proxy folder destination host " + proxyFolderDestinationHost + " not found.");
       }
       return retVal;
    }

    /**
     * Add a new ProxyFolder to the context.
     *
     * @param proxyFolder the ProxyFolderBean to add
     * @throws IllegalArgumentException if the context is already registered
     * @throws NullArgumentException if the argument is null
     */
    public void addProxyFolder(ProxyFolderBean proxyFolder) throws IllegalArgumentException, NullArgumentException {
        if (proxyFolder == null) {
            log.warning("Null ProxyFolderBean parameter.. Ignoring operation");
            throw new NullArgumentException("Null ProxyFolderBean parameter.. Ignoring operation");
        } else {
            boolean exists = proxyFoldersByURLEncodedName.containsKey(proxyFolder.getURLEncodedFolderName());
            if (exists) {
                log.warning("The Proxy foder already exists.. nothing done.");
                throw new IllegalArgumentException("The Proxy foder already exists.. nothing done.");
            } else {
                log.finer("Adding Proxy folder " + proxyFolder.getURLEncodedFolderName());
                proxyFoldersByURLEncodedName.put(proxyFolder.getURLEncodedFolderName(), proxyFolder);

                //add the proxy-folder to the second indexing map.
                String destinationHost = URLUtils.getDestinationHost(proxyFolder.getDestinationAsURL());
                LinkedList<ProxyFolderBean> currentSlot = null;
                if (proxyFoldersByDestinationHost.containsKey(destinationHost)) {
                    currentSlot = proxyFoldersByDestinationHost.get(destinationHost);
                    currentSlot.add(proxyFolder);
                } else {
                    currentSlot = new LinkedList();
                    currentSlot.add(proxyFolder);
                    proxyFoldersByDestinationHost.put(destinationHost, currentSlot);
                }
            }
        }
    }

    /**
     * Remove a ProxyFolder from the context
     * @param proxyFolder the proxyFolder to remove.
     * @throws IllegalArgumentException if the context doesn't exist
     * @throws NullArgumentException if the argument is null
     */
    public void removeProxyFolder (ProxyFolderBean proxyFolder) throws IllegalArgumentException, NullArgumentException {
       if (proxyFolder == null) {
            log.warning("Null ProxyFolderBean parameter.. Ignoring operation");
            throw new NullArgumentException("Null ProxyFolderBean parameter.. Ignoring operation");
        } else {
            boolean exists = proxyFoldersByURLEncodedName.containsKey(proxyFolder.getURLEncodedFolderName());
            if (!exists) {
                log.warning("The Proxy foder doesn't exists.. nothing done.");
                throw new IllegalArgumentException("The Proxy foder doesn't exists.. nothing done.");
            } else {
                log.finer("Deleting existing Proxy folder " + proxyFolder.getFolderName());
                proxyFoldersByURLEncodedName.remove(proxyFolder.getURLEncodedFolderName());

                //Delete the proxy-folder from the second indexing map.
                String destinationHost = URLUtils.getDestinationHost(proxyFolder.getDestinationAsURL());
                LinkedList<ProxyFolderBean> currentSlot = null;
                currentSlot = proxyFoldersByDestinationHost.get(destinationHost);
                if (currentSlot.size() == 1) {
                    currentSlot.remove(proxyFolder);
                    proxyFoldersByDestinationHost.remove(destinationHost);
                } else {
                    Iterator<ProxyFolderBean> iterator = currentSlot.iterator();
                    while (iterator.hasNext()) {
                        ProxyFolderBean curFolder = iterator.next();
                        if (curFolder == proxyFolder)
                            iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Updates Context URLEncoded Indexes.<br/>
     * This method is invoked from a proxy folder that has changed its name 
     * in order to update the internal index of the registered
     * proxy folders.
     *
     * @param theFolder the folder that has jus been updated
     */
    protected void updateFolderURLEncodedIndex (String oldURLEncodedName, ProxyFolderBean theFolder) {
        //remove the proxyFolder form the index using its unique urlEncodedName
        this.proxyFoldersByURLEncodedName.remove(oldURLEncodedName);

        //readd the folder to the index using the new unique urlEncodedName
        this.proxyFoldersByURLEncodedName.put(theFolder.getURLEncodedFolderName(), theFolder);
    }

    /**
     * Updates Context Destination Indexes.<br/>
     * This method is invoked from a proxy folder that has changed its
     * destination in order to keep aligned the internal index of the
     * registered proxy folders.
     *
     * @param theFolder the folder that has jus been updated
     */
    protected void updateFolderDestinationIndex (URL oldDestination, ProxyFolderBean theFolder) {
        //Remove the proxy-folder from the second indexing map.
        String destinationHost = URLUtils.getDestinationHost(oldDestination);
        LinkedList<ProxyFolderBean> currentSlot = null;
        currentSlot = proxyFoldersByDestinationHost.get(destinationHost);
        if (currentSlot.size() == 1) {
            currentSlot.remove(theFolder);
            proxyFoldersByDestinationHost.remove(destinationHost);
        } else {
            Iterator<ProxyFolderBean> iterator = currentSlot.iterator();
            while (iterator.hasNext()) {
                ProxyFolderBean curFolder = iterator.next();
                if (curFolder == theFolder)
                    iterator.remove();
            }
        }

        //Re-Add the proxy folder with the new destination host
        destinationHost = URLUtils.getDestinationHost(theFolder.getDestinationAsURL());
        currentSlot = null;
        if (proxyFoldersByDestinationHost.containsKey(destinationHost)) {
            currentSlot = proxyFoldersByDestinationHost.get(destinationHost);
            currentSlot.add(theFolder);
        } else {
            currentSlot = new LinkedList();
            currentSlot.add(theFolder);
            proxyFoldersByDestinationHost.put(destinationHost, currentSlot);
        }
    }

    /**
     * Get a collection of all the proxy folders into the context
     * @return a Collection of ProxyFolders
     */
    public Collection<ProxyFolderBean> getProxyFoldersAsCollection () {
        return proxyFoldersByURLEncodedName.values();
    }

    /**
     * Returns the number of proxy folders handled by the context.
     * @return the number of proxy folders into the context.
     */
    public int getProxyFoldersCount () {
        return proxyFoldersByURLEncodedName.size();
    }

    /**
     * Get a single value paramenter from the configuration
     *
     * @param parameterName the parameter name
     * @see ParameterTags
     * @return the parameter value
     *
     */
    public String getSingleValueParameter(String parameterXPath) {
        String retVal = config.getString(parameterXPath);
        log.finer("Getting single value of parameter on " + parameterXPath + ": " + retVal);
        return retVal;
    }

    /**
     * Get a list of values for a parameter from the configuration
     *
     * @param parameterName the parameter name
     * @see ParameterTags
     * @return the parameter value
     * @throws IllegalArgumentException if the parameter is not a single value parameter.
     */
    public Collection<String> getMultiValueParameter(String parameterXPath) {
        Collection<String> retValue = config.getList(parameterXPath);
        if (!(retValue instanceof Collection)) {
            log.warning("Parameter on " + parameterXPath + " is not multivalue");
        } else if (retValue.isEmpty()) {
            log.warning("Parameter on " + parameterXPath + " do not have any value");
        } else {
            log.finer("Multiple value parameter on " + parameterXPath + " loaded [" + retValue.size() + " elements]");
        }
        return retValue;
    }

    /**
     * Standard getter method to obtain the current log level.
     *
     * @return the log level as defined into the java standard Logger.
     * Possible values are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
     */
    public String getLogLevel() {
        return log.getLevel().toString();
    }

    /**
     * Standard setter method to set the new log level to use at run time.
     *
     * @param the new log level as defined into the java standard Logger.
     * Possible values are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
     */
    public String setLogLevel(String logLevel) {
        String newLogLevel = null;
        if (logLevel == null) {
           log.warning("Null loglevel.. setting it to " + ProxymaTags.UNSPECIFIED_LOGLEVEL);
           newLogLevel = ProxymaTags.UNSPECIFIED_LOGLEVEL;
        } else if (ProxymaTags.LogLevels.ALL.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.FINEST.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.FINER.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.FINE.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.CONFIG.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.INFO.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.WARNING.toString().equals(logLevel) ||
            ProxymaTags.LogLevels.SEVERE.toString().equals(logLevel)) {
            newLogLevel = logLevel;
        } else {
           log.warning("Unknown log level \"" + logLevel + "\" setting it to " + ProxymaTags.UNSPECIFIED_LOGLEVEL);
           newLogLevel = ProxymaTags.UNSPECIFIED_LOGLEVEL;
        }
        ProxymaLoggersUtil.updateLogLevel(log, newLogLevel);
        return newLogLevel;
    }

    /**
     * Get the logger for this context instance.
     * This function provides a simple way to allow any plugin to attach its
     * own logs to the proxyma main log logFile.
     *
     * @return the context logger
     */
    public Logger getLogger() {
        return this.log;
    }


    /**
     * Get the name of this ccontext
     * @return the context name as String
     */
    public String getName() {
        return contextName;
    }

    /**
     * Get the default encoding charset used to encode/decode URLs and to parse html files.
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Get the curren relese nubler of Proxyma-core library
     * @return the default encoding
     */
    public String getProxymaVersion() {
        return proxymaVersion;
    }

    /**
     * Get the contextPath for this context
     * @return the contextURI as String
     */
    public String getProxymaContextBasePath() {
        return proxymaContextBasePath;
    }

    /**
     * Returns the path of the logs directory for this context.
     * @return the directory of the logs.
     */
    public String getLogsDirectoryPath () {
        return logsDirectoryPath;
    }

    /**
     * The context name.
     */
    private String contextName = null;

    /**
     * The context base path.
     */
    private String proxymaContextBasePath = null;

    /**
     * The registered ProxyFolders for this context indexed by name
     * @see ProxyFolder
     */
    private ConcurrentHashMap<String, ProxyFolderBean> proxyFoldersByURLEncodedName = null;

    /**
     * The registered ProxyFolders for this context indexed by destination
     * @see ProxyFolder
     */
    private ConcurrentHashMap<String, LinkedList<ProxyFolderBean>> proxyFoldersByDestinationHost = null;

    /**
     * The Global Configuration managed by "Commons Configuration" component.
     */
    private XMLConfiguration config = null;

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * The main directory where the logs for this context will be written.
     */
    private String logsDirectoryPath = null;

    /**
     * The default encodig to use to encode/decode URLs and for html parsing
     */
    private String defaultEncoding = null;

    /**
     * The current release of proxyma
     */
    private String proxymaVersion = null;
}

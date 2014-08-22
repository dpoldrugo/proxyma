package m.c.m.proxyma;

/**
 * <p>
 * This class is only a constants aggregator
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaTags.java 157 2010-06-27 19:24:02Z marcolinuz $
 */
public class ProxymaTags {
    //Resource Handler available Types
    public static enum HandlerType { PREPROCESSOR, RETRIVER, TRANSFORMER, SERIALIZER };
    public static enum LogLevels {SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL };

    // Global Configuration Parameter names
    public static final String DEFAULT_LOGGER_PREFIX = "m.c.m.proxyma";
    public static final String GLOBAL_DEFAULT_ENCODING = "global/defaultEncoding";
    public static final String CONFIG_FILE_VERSION = "global/version";
    public static final String GLOBAL_BUFFERS_IMPLEMENTATION = "global/byteBufferImplementation";
    public static final String GLOBAL_SHOW_FOLDERS_LIST = "global/showProxyFoldersOnRootPath";
    public static final String GLOBAL_LOGLEVEL = "global/logging/@level";
    public static final String GLOBAL_LOGFILE_MAXSIZE = "global/logging/@maxLinesPerFile";
    public static final String GLOBAL_LOGFILES_RETENTION = "global/logging/@retentionPolicy";

    //Plugins Parameter configuration names
    public static final String AVAILABLE_CACHE_PROVIDERS = "plugins/avaliableCacheProviders/cacheProvider";
    public static final String AVAILABLE_PREPROCESSORS = "plugins/avaliablePreprocessors/resourceHandler";
    public static final String AVAILABLE_TRANSFORMERS = "plugins/avaliableTransformers/resourceHandler";
    public static final String AVAILABLE_SERIALIZERS = "plugins/availableSerializers/resourceHandler";
    public static final String AVAILABLE_RETRIVERS = "plugins/availableRetrivers/resourceHandler";

    //Context configuration parameter names
    public static final String FOLDER_MAX_POST_SIZE = "defaultContext/folderSettings/@maxPostSize";
    public static final String FOLDER_ENABLED = "defaultContext/folderSettings/@enabled";
    public static final String FOLDER_PREPROCESSORS = "defaultContext/folderSettings/preprocessors/resourceHandler/@class";
    public static final String FOLDER_CACHEPROVIDER = "defaultContext/folderSettings/cacheProvider/@class";
    public static final String FOLDER_RETRIVER = "defaultContext/folderSettings/retriver/@class";
    public static final String FOLDER_TRANSFORMERS = "defaultContext/folderSettings/transformers/resourceHandler/@class";
    public static final String FOLDER_SERIALIZER = "defaultContext/folderSettings/serializer/@class";

    //Misc constants
    public static final int UNSPECIFIED_POST_SIZE = 5000;
    public static final String UNSPECIFIED_CACHEPROVIDER = "m.c.m.proxyma.plugins.caches.NullCacheProvider";
    public static final String UNSPECIFIED_RETRIVER = "m.c.m.proxyma.plugins.retrivers.TestPageRetriver";
    public static final String UNSPECIFIED_SERIALIZER = "m.c.m.proxyma.plugins.serializers.SimpleSerializer";
    public static final String UNSPECIFIED_LOGLEVEL = "INFO";
}

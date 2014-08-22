package m.c.m.proxyma;

/**
 * <p>
 * This is a simple Constants-container class.
 *
 * NOTE: This is only the hook to the reverse-proxy engine. If you are looking
 *       for proxyma intercafe take a look to the proxyma-console webapp.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: GlobalConstants.java 165 2010-06-30 20:09:51Z marcolinuz $
 */
public class GlobalConstants {

    /**
     * The name of the jsp that manages the context overviwe list.
     */
    public static final String CONTEXT_OVERVIEW_PAGE = "/contextOverview.jsp";

    /**
     * The name of the jsp that manages the proxy folder configurations
     */
    public static final String EDIT_PROXY_FOLDER_PAGE = "/editProxyFolder.jsp";

    /**
     * The name of the Jsp that manages the context selection
     */
    public static final String WELCOME_PAGE = "/index.jsp";

    /**
     * This parameter specifies the proxy folder to process
     */
    public static final String TARGET_PARAMETER = "target";

    /**
     * Only an empty string
     */
    public static final String EMPTY_STRING = "";

    /**
     *This parmeter specifies to the console the context to handle.
     */
    public static final String CONTEXT_PARAMETER = "context";

    /**
     * This parameter tells to the console the command to execute
     */
    public static final String COMMAND_PARAMETER = "command";

    //POSSIBLE COMMANDS
    public static final String IMPORT_CONTEXT_COMMAND = "importProxyFolders";
    public static final String EXPORT_CONTEXT_COMMAND = "exportProxyFolders";
    public static final String ADD_NEW_FOLDER_COMMAND = "addNewProxyFolder";
    public static final String MANAGE_FOLDER_COMMAND = "updateProxyFolder";
    public static final String UPDATE_FOLDER_COMMAND = "editProxyFolder";
    public static final String RELOAD_PAGE_COMMAND = "reloadPage";
    public static final String CHANGE_LOG_LEVEL_COMMAND = "changeLogLevel";


    /**
     * This parameter specifies the sub-command to execute
     */
    public static final String ACTION_PARAMETER = "action";

    //POSSIBLE ACTIONS (sub-commands)
    public static final String ENABLE_FOLDER_ACTION = "enableProxyFolder";
    public static final String DISABLE_FOLDER_ACTION = "disableProxyFolder";
    public static final String GO_TO_EDIT_FOLDER_ACTION = "goToEditProxyFolder";
    public static final String DELETE_FOLDER_ACTION = "deleteProxyFolder";
    public static final String RELOAD_OVERVIEW_ACTION = "reloadOverview";
    public static final String GOTO_WELCOME_PAGE_ACTION = "reloadWelcome";

    /**
     * This is the request attribute forwarded to the jsp that countains the
     * current context.
     */
    public static final String CONTEXT_REQUEST_ATTRIBUTE = "contextAttr";

    /**
     * This is the request attribute forwarded to the jsp that countains the
     * current proxy folder to show.
     */
    public static final String PROXY_FOLDER_REQUEST_ATTRIBUTE = "proxyFolderAttr";

    /**
     * This is the request attribute forwarded to the jsp that countains the
     * collection of the proxy folders into the context.
     */
    public static final String PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE = "proxyFolderListAttr";

    /**
     * This is the request attribute forwarded to the jsp that countains the
     * message to show to the user.
     */
    public static final String USER_MESSAGE_REQUEST_ATTRIBUTE = "userMessage";

    /**
     * This is the request attribute forwarded to the jsp that countains a
     * collection of available preprocessor plugins.
     */
    public static final String AVAILABLE_PREPROCESSORS_ATTRIBUTE = "availablePreprocessors";

    /**
     * This is the request attribute forwarded to the jsp that countains a
     * collection of available cache provider plugins.
     */
    public static final String AVAILABLE_CACHE_PROVIDERS_ATTRIBUTE = "availableCacheProviders";

    /**
     * This is the request attribute forwarded to the jsp that countains a
     * collection of available retriver plugins.
     */
    public static final String AVAILABLE_RETRIVERS_ATTRIBUTE = "availableRetrivers";

    /**
     * This is the request attribute forwarded to the jsp that countains a
     * collection of available cache transformer plugins.
     */
    public static final String AVAILABLE_TRANSFORMERS_ATTRIBUTE = "availableTransformers";

    /**
     * This is the request attribute forwarded to the jsp that countains a
     * collection of available cache serializer plugins.
     */
    public static final String AVAILABLE_SERIALIZERS_ATTRIBUTE = "availableSerializers";

    /**
     * Default value for new blank proxyfoleder name
     */
    public static final String BLANK_PROXY_FOLDER_NAME = "proxymaHome";

    /**
     * Default value for new blank proxyfoleder destination
     */
    public static final String BLANK_PROXY_FOLDER_DESTINATION = "http://proxyma.sourceforge.net";

    //Edit Proxy Folder FORM Parameter Names
    public static final String EDIT_FORM_FOLDER_NAME = "proxyFolderName";
    public static final String EDIT_FORM_DESTINATION = "destination";
    public static final String EDIT_FORM_MAX_POST_SIZE = "maxPostSize";
    public static final String EDIT_FORM_CACHE_PROVIDER = "cacheProvider";
    public static final String EDIT_FORM_RETRIVER = "retriver";
    public static final String EDIT_FORM_SERIALIZER = "serializer";
    public static final String EDIT_FORM_PREPROCESSORS = "proprocessors";
    public static final String EDIT_FORM_TRANSFORMERS = "transformers";
}

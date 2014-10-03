package m.c.m.proxyma;

import java.io.UnsupportedEncodingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ProxyEngine;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This servlet manages all the requests that comes form the proxyma-console pages.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaConsoleServlet.java 169 2010-07-02 21:11:21Z marcolinuz $
 */
public class ProxymaConsoleServlet extends HttpServlet {

    /**
     * This command retrives configuration parameters from the web.xml and
     * initialize the private members of the servlet.
     *
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init() throws ServletException {
        //Create a new proxyma facade
        this.proxyma = new ProxymaFacade();
    }

    /**
     * This command handles the GET requests.
     *
     * @param httpservletrequest  the request..
     * @param httpservletresponse the response..
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    public void doGet(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException {

        doPost(httpservletrequest, httpservletresponse);
    }

    /**
     * This command handles the POST requests.
     *
     * @param httpservletrequest  the requrest..
     * @param httpservletresponse the response..
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    public void doPost(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException {

        //Getting parameters
        String context = httpservletrequest.getParameter(GlobalConstants.CONTEXT_PARAMETER);
        String target = httpservletrequest.getParameter(GlobalConstants.TARGET_PARAMETER);
        String action = httpservletrequest.getParameter(GlobalConstants.ACTION_PARAMETER);
        String command = httpservletrequest.getParameter(GlobalConstants.COMMAND_PARAMETER);
        
        if (context == null && target == null && action == null && command == null) {
        	context = "default";
        	target = "none";
        	action = "reloadOverview";
        	command = "reloadPage";
        }

        if (!(validateParameters(context, command, target, action))) {
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Choose what to do..
        //manages updateRule command
        ProxymaContext currentContext = proxyma.getContextByName(context);
        ProxyFolderBean currentTarget = proxyma.getProxyFolderByURLEncodedName(target, currentContext);
        if (GlobalConstants.MANAGE_FOLDER_COMMAND.equals(command)) {
            //Update or modify an existent rule
            if (GlobalConstants.ENABLE_FOLDER_ACTION.equals(action)) {
                //enble a disabled rule
                proxyma.enableProxyFolder(currentTarget);
                httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
                httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, "Proxy-Folder \"" + currentTarget.getFolderName() + "\" successfully Unlocked.");
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            } else if (GlobalConstants.DISABLE_FOLDER_ACTION.equals(action)) {
                //disable an enabled rule
                proxyma.disableProxyFolder(currentTarget);
                httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
                httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, "Proxy-Folder \"" + currentTarget.getFolderName() + "\" successfully Locked.");
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            } else if (GlobalConstants.GO_TO_EDIT_FOLDER_ACTION.equals(action)) {
                //Edit an existent rule
                if (currentTarget != null) {
                    ProxyEngine dummyProxyEngine = null;
                    try {
                        //Load a dummy proxy engine to get the list of the available plugins
                        dummyProxyEngine = this.proxyma.createNewProxyEngine(currentContext);
                    } catch (IllegalAccessException ex) {
                        throw new ServletException("Unable to get available plugins for context " + currentContext.getName());
                    }

                    httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_REQUEST_ATTRIBUTE, currentTarget);
                    httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_CACHE_PROVIDERS_ATTRIBUTE, dummyProxyEngine.getRegisteredCachePlugins());
                    httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_PREPROCESSORS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.PREPROCESSOR));
                    httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_RETRIVERS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.RETRIVER));
                    httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_TRANSFORMERS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.TRANSFORMER));
                    httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_SERIALIZERS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.SERIALIZER));
                    forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.EDIT_PROXY_FOLDER_PAGE, currentContext);
                } else {
                    httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else if (GlobalConstants.DELETE_FOLDER_ACTION.equals(action)) {
                //delete an existent rule
                try {
                    proxyma.removeProxyFolder(currentTarget, currentContext);
                    httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, "Proxy-Folder \"" + currentTarget.getFolderName() + "\" successfully deleted from context \"" + currentContext.getName() + "\".");
                } catch (Exception e) {
                    httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, e.getMessage());
                }

                httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            } else {
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            //Manages refreshPage command
        } else if (GlobalConstants.RELOAD_PAGE_COMMAND.equals(command)) {
            //Refresh page data
            if (GlobalConstants.RELOAD_OVERVIEW_ACTION.equals(action)) {
                httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            } else if (GlobalConstants.GOTO_WELCOME_PAGE_ACTION.equals(action)) {
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.WELCOME_PAGE, currentContext);
            } else {
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            //Manages addRule command by forwarding to the edit rule page
        } else if (GlobalConstants.ADD_NEW_FOLDER_COMMAND.equals(command)) {
            //Add a new Rule
            ProxyFolderBean blankFolder = null;
            ProxyEngine dummyProxyEngine = null;
            try {
                //Load a dummy proxy engine to get the list of the available plugins
                dummyProxyEngine = this.proxyma.createNewProxyEngine(currentContext);
                blankFolder = proxyma.createNewProxyFolder(GlobalConstants.BLANK_PROXY_FOLDER_NAME, GlobalConstants.BLANK_PROXY_FOLDER_DESTINATION, currentContext);
            } catch (IllegalAccessException ex) {
                throw new ServletException("Unable to get available plugins for context " + currentContext.getName());
            } catch (IllegalArgumentException ex) {
                //The default folder already exists open it
                blankFolder = proxyma.getProxyFolderByURLEncodedName(GlobalConstants.BLANK_PROXY_FOLDER_NAME, currentContext);
            }
            httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_CACHE_PROVIDERS_ATTRIBUTE, dummyProxyEngine.getRegisteredCachePlugins());
            httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_PREPROCESSORS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.PREPROCESSOR));
            httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_RETRIVERS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.RETRIVER));
            httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_TRANSFORMERS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.TRANSFORMER));
            httpservletrequest.setAttribute(GlobalConstants.AVAILABLE_SERIALIZERS_ATTRIBUTE, dummyProxyEngine.getRegisteredPluginsByType(ProxymaTags.HandlerType.SERIALIZER));

            httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_REQUEST_ATTRIBUTE, blankFolder);
            forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.EDIT_PROXY_FOLDER_PAGE, currentContext);
            //todo: NOT YET IMPLEMENTED, will manage massive rules import from DB, XML, file.. etc...
        } else if (GlobalConstants.IMPORT_CONTEXT_COMMAND.equals(command)) {
            //Import rules from external sources
            httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, "Function not yet Implemented :O(");
            httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
            forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            return;
            //todo: NOT YET IMPLEMENTED, will manage massive rules export to DB, XML, file.. etc...
        } else if (GlobalConstants.EXPORT_CONTEXT_COMMAND.equals(command)) {
            //Import rules from external sources
            httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, "Function not yet Implemented :O(");
            httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
            forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            return;
            //Manages edit page requests.
        } else if (GlobalConstants.UPDATE_FOLDER_COMMAND.equals(command)) {
            //Update an existent Rule
            try {
                StringBuffer message = new StringBuffer(256);
                message.append("Proxy-Folder \"");
                message.append(currentTarget.getFolderName());
                message.append("\" successfully updated.");
                if (!currentTarget.isEnabled())
                    message.append("<br/>WARNING: The folder is locked, click on the \"Status\" button to unlock it.");
                updateProxyFolderFromRequestParameters(httpservletrequest, currentTarget);
                httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, message.toString());
                httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
            } catch (Exception e) {
                httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, e.getMessage());
                httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_REQUEST_ATTRIBUTE, currentTarget);
                forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.EDIT_PROXY_FOLDER_PAGE, currentContext);
            }
        } else if (GlobalConstants.CHANGE_LOG_LEVEL_COMMAND.equals(command)) {
            String newLevel = currentContext.setLogLevel(action);
            httpservletrequest.setAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE, "Changed log level for context \"" + currentContext.getName() + "\" to " + newLevel);
            httpservletrequest.setAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE, proxyma.getContextProxyFolders(currentContext));
            forwardToJsp(httpservletrequest, httpservletresponse, GlobalConstants.CONTEXT_OVERVIEW_PAGE, currentContext);
        } else {
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    /**
     * This method updates the passed ProxyFolder using the request parameters
     *
     * @param request the servlet request passed from edtit_rule.jsp
     * @return the ruleBean if all parameters are valid.
     */
    private void updateProxyFolderFromRequestParameters(HttpServletRequest request, ProxyFolderBean theFolder)
            throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {

        //Obtain request parmeters
        String proxyFolderName = request.getParameter(GlobalConstants.EDIT_FORM_FOLDER_NAME);
        String destination = request.getParameter(GlobalConstants.EDIT_FORM_DESTINATION);
        String maxPostSize = request.getParameter(GlobalConstants.EDIT_FORM_MAX_POST_SIZE);
        String cacheProvider = request.getParameter(GlobalConstants.EDIT_FORM_CACHE_PROVIDER);
        String retriver = request.getParameter(GlobalConstants.EDIT_FORM_RETRIVER);
        String serializer = request.getParameter(GlobalConstants.EDIT_FORM_SERIALIZER);
        String[] preprocessors = request.getParameterValues(GlobalConstants.EDIT_FORM_PREPROCESSORS);
        String[] transformers = request.getParameterValues(GlobalConstants.EDIT_FORM_TRANSFORMERS);

        //updtate folder name
        theFolder.setFolderName(proxyFolderName);

        //Update destination
        theFolder.setDestination(destination);

        //update max Post Size
        theFolder.setMaxPostSize(Integer.parseInt(maxPostSize));
        
        //Update cahce provider class
        theFolder.setCacheProvider(cacheProvider);

        //update retriver class
        theFolder.setRetriver(retriver);

        //update serializer class
        theFolder.setSerializer(serializer);

        //Update preprocessors classes
        theFolder.getPreprocessors().removeAll(theFolder.getPreprocessors());
        if (preprocessors != null) {
            for (int i=0; i< preprocessors.length; i++)
                theFolder.registerPreprocessor(preprocessors[i]);
        }
        
        //Update transformer classes
        theFolder.getTransformers().removeAll(theFolder.getTransformers());
        if (transformers != null) {
            for (int i=0; i< transformers.length; i++)
                theFolder.registerTransformer(transformers[i]);
        }
    }

    /**
     * Validate input from client
     *
     * @param context proxyma context to manage
     * @param command command request parameter
     * @param target target request parameter
     * @param action action request parameter
     * @return true if all required parameters were passed.
     */
    private boolean validateParameters(String context, String method, String target, String action) {
        boolean retValue = true;

        if ((method == null) || GlobalConstants.EMPTY_STRING.equals(method.trim()))
            retValue = false;

        if ((target == null) || GlobalConstants.EMPTY_STRING.equals(target.trim()))
            retValue = false;

        if ((action == null) || GlobalConstants.EMPTY_STRING.equals(action.trim()))
            retValue = false;

        if ((context == null) || GlobalConstants.EMPTY_STRING.equals(context.trim()))
            retValue = false;

        if (proxyma.getContextByName(context) == null)
            retValue = false;

        return retValue;
    }


    /**
     * Utility method to forward the flow to a jsp..
     *
     * @param httpservletrequest     the request
     * @param httpservletresponse    the response
     * @param page                   the page to forward
     * @param proxymaInstanceContext the proxymaContextPath
     */
    private void forwardToJsp(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse,
                              String page, ProxymaContext context) throws IOException, ServletException {

        httpservletrequest.setAttribute(GlobalConstants.CONTEXT_REQUEST_ATTRIBUTE, context);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(page);
        dispatcher.forward(httpservletrequest, httpservletresponse);
    }

        /**
     * The instance of the proxyma-core facade class that will be used to
     * setup the reverse proxy environment
     */
    private ProxymaFacade proxyma = null;

    /**
     * The reverse proxy engine used to handle the client requests
     */
    private ProxyEngine proxymaEngine = null;
}

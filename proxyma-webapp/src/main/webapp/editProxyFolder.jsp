<%@ page import="m.c.m.proxyma.context.ProxyFolderBean,
         m.c.m.proxyma.ProxymaTags,
         m.c.m.proxyma.context.ProxymaContext,
         m.c.m.proxyma.GlobalConstants,
         m.c.m.proxyma.plugins.caches.CacheProvider,
         m.c.m.proxyma.core.ResourceHandler,
         java.util.Collection,
         java.util.Iterator" %>
<!--
    Document   : editProxyFolder.jsp
    Description:
       This is the proxy-folder management page.

    NOTE:
       this software is released under GPL License.
       See the LICENSE of this distribution for more informations.

       @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
       @version $Id: editProxyFolder.jsp 169 2010-07-02 21:11:21Z marcolinuz $
-->
<html>
    <%
                String proxymaContextPath = getServletContext().getContextPath();
                ProxymaContext context = (ProxymaContext) request.getAttribute(GlobalConstants.CONTEXT_REQUEST_ATTRIBUTE);
                ProxyFolderBean currentFolder = (ProxyFolderBean) request.getAttribute(GlobalConstants.PROXY_FOLDER_REQUEST_ATTRIBUTE);
                Collection<CacheProvider> cacheProviders = (Collection<CacheProvider>) request.getAttribute(GlobalConstants.AVAILABLE_CACHE_PROVIDERS_ATTRIBUTE);
                Collection<ResourceHandler> preprocessors = (Collection<ResourceHandler>) request.getAttribute(GlobalConstants.AVAILABLE_PREPROCESSORS_ATTRIBUTE);
                Collection<ResourceHandler> retrivers = (Collection<ResourceHandler>) request.getAttribute(GlobalConstants.AVAILABLE_RETRIVERS_ATTRIBUTE);
                Collection<ResourceHandler> transformers = (Collection<ResourceHandler>) request.getAttribute(GlobalConstants.AVAILABLE_TRANSFORMERS_ATTRIBUTE);
                Collection<ResourceHandler> serializers = (Collection<ResourceHandler>) request.getAttribute(GlobalConstants.AVAILABLE_SERIALIZERS_ATTRIBUTE);
    %>
    <head>
        <title>Proxyma Configuration Console</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link href="<%=proxymaContextPath%>/stile.css" rel="stylesheet" type="text/css">
        <script type="text/javascript">
            //Shows or hides divs based upon the content of the section box
            var showAdvanced = false;

            function toggle(divName)
            {
                if (showAdvanced)
                    hideDiv(divName);
                else
                    showDiv(divName);
            }

            //Hides a div
            function hideDiv(divName)
            {
                if (document.getElementById)
                {
                    // DOM3 = IE5, NS6
                    var theDiv = document.getElementById(divName);
                    theDiv.style.display = 'none';
                    theDiv.style.visibility = 'hidden';
                    showAdvanced = false;
                }
            }

            //Shows a Div
            function showDiv(divName) {
                if (document.getElementById)
                {
                    // DOM3 = IE5, NS6
                    theDiv = document.getElementById(divName);
                    theDiv.style.visibility = 'visible';
                    theDiv.style.display = 'inline';
                    showAdvanced = true;
                }
            }
        </script>
    </head>
    <body onload="hideDiv('advanced')">
        <jsp:include page="/header.html" />
        <div id="path" class="percorso">
            You are Here: <a href="<%=proxymaContextPath%>">Context Selection</a> -> <a href="<%=proxymaContextPath%>/console?<%=GlobalConstants.COMMAND_PARAMETER%>=<%=GlobalConstants.RELOAD_PAGE_COMMAND%>&<%=GlobalConstants.TARGET_PARAMETER%>=null&<%=GlobalConstants.ACTION_PARAMETER%>=<%=GlobalConstants.RELOAD_OVERVIEW_ACTION%>&<%=GlobalConstants.CONTEXT_PARAMETER%>=<%=context.getName()%>"><%=context.getName()%> context</a> -> Edit Proxy Folder
        </div>
        <br />
        <div id="centrale">
            <div class="tableTitle">
                Edit Proxy-Folder
            </div>
            <div id="gruppo">
                <%
                            String message = (String) request.getAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE);
                            if (message != null) {
                %>
                <div class="message">
                    <font color="red"><center><b><%=message%></b></center></font>
                </div>
                <%
                            }
                %>
                <form action="<%=proxymaContextPath%>/console" method="post" >
                    <input type="hidden" name="<%=GlobalConstants.CONTEXT_PARAMETER%>" value="<%=context.getName()%>" />
                    <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER%>" value="<%=GlobalConstants.UPDATE_FOLDER_COMMAND%>" />
                    <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER%>" value="<%=currentFolder.getURLEncodedFolderName()%>" />
                    <input type="hidden" name="<%=GlobalConstants.ACTION_PARAMETER%>" value="null" />
                    <div id="required">
                        <table width="95%" align="center">
                            <tr class="labels">
                                <td align="center" width="10%" >
                                    <b>Parameter Name</b>
                                </td>
                                <td align="left" width="40%" >
                                    <b>Parameter Value</b>
                                </td>
                                <td align="left" width="50%" >
                                    <b>Parameter Description</b>
                                </td>
                            </tr>
                            <tr class="even">
                                <td align="center">
	                                Proxy Folder Name <font color=red>(required)</font>
                                </td>
                                <td align="left">
                                    <input type="text" class="risposta" name="<%=GlobalConstants.EDIT_FORM_FOLDER_NAME%>" size="40" maxlength=60" value="<%=currentFolder.getFolderName()%>"/>
                                </td>
                                <td align="left">
	                                In this field you can set the name and the consequent path of the Proxy-Folder.<br/>
	                                This is a required parameter but it can't countain any "/" character.<br/>
	                                Note: the destination will be masqueraded into the follow path: "<%=context.getProxymaContextBasePath()%>/${URLEncoded-ProxyFolderName}".
                                </td>
                            </tr>
                            <tr class="odd">
                                <td align="center">
	                                Destination Host/Path <font color=red>(required)</font>
                                </td>
                                <td align="left">
                                    <input type="text" class="risposta" name="<%=GlobalConstants.EDIT_FORM_DESTINATION%>" size="40" maxlength="255" value="<%=currentFolder.getDestinationAsString()%>" />
                                </td>
                                <td align="left">
	                                This is another required parameter and it must countain the valid URL that will be masquerded by Proxyma.<br/>
	                                NOTE: If provided, the tailing "/" (slash) will be automatically removed.
                                </td>
                            </tr>
                            <tr class="even">
                                <td align="center" width="10%">
	                                Max POST-Size
                                </td>
                                <td align="left" width="40%">
                                    <input type="text" class="risposta" name="<%=GlobalConstants.EDIT_FORM_MAX_POST_SIZE%>" size="40" maxlength="50" value="<%=currentFolder.getMaxPostSize()%>" />
                                </td>
                                <td align="left" width="50%">
	                                This option sets the maximum allowed size for a "POST" method.<br/>
	                                In some cases this parameter could be useful to avoid some kind of DOS attaks based upon large POSTS (that cause high memory consumption).<br/>
	                                Note: To disable this limit, set it to 0 (zero).
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div id="toggleView">
                        <table width="95%" align="center">
                            <tr class="submit">
                                <td width="100%" align="center" >
                                    <input type="button" name="toggleButton" value="Show/Hide Plugins Options" onclick="toggle('advanced');return(false);" />
                                </td>
                            </tr>
                       </table>
                    </div>
                    <div id="advanced">
                        <div class="tableTitle">
                            Proxy-Engine Availabe Plugins
                        </div>
                        <table width="95%" align="center">
                            <tr class="labels">
                                <td align="center" width="10%" >
                                    <b>Plugin Type</b>
                                </td>
                                <td align="left" width="40%" >
                                    <b>Plugins Selection</b>
                                </td>
                                <td align="left" width="50%" >
                                    <b>Plugins Description</b>
                                </td>
                            </tr>                           
                            <tr class="odd">
                                <td align="center">
	                                Preprocessors
                                </td>
                                <td align="left">
                                    <%
                                        ResourceHandler currentHandler = null;
                                        Iterator<ResourceHandler> handlerIterator = preprocessors.iterator();
                                        Iterator <String>selectedHandlers = null;
                                        while (handlerIterator.hasNext()) {
                                            currentHandler = handlerIterator.next();
                                            selectedHandlers = currentFolder.getPreprocessors().iterator();
                                            boolean found=false;
                                            while(selectedHandlers.hasNext())
                                                if (currentHandler.getClass().getCanonicalName().equals(selectedHandlers.next()))
                                                    found=true;

                                            if (found) {
                                            %>
                                                <div class="checkbox"><input type="checkbox" name="<%=GlobalConstants.EDIT_FORM_PREPROCESSORS%>" value="<%=currentHandler.getClass().getName()%>" checked="true"/><%=currentHandler.getName()%></div>
                                            <%
                                            } else {
                                            %>
                                                <div class="checkbox"><input type="checkbox" name="<%=GlobalConstants.EDIT_FORM_PREPROCESSORS%>" value="<%=currentHandler.getClass().getName()%>"/><%=currentHandler.getName()%></div>
                                            <%
                                            }
                                        }
                                    %>
                                </td>
                                <td align="left">
                                    <ul>
                                        <%
                                            handlerIterator = preprocessors.iterator();
                                            while (handlerIterator.hasNext()) {
                                                currentHandler = handlerIterator.next();
                                        %>
                                        <li>
                                            <b><%=currentHandler.getName()%>:</b> <%=currentHandler.getHtmlDescription()%>
                                        </li><br/>
                                        <%  }  %>
                                    </ul>
                                </td>
                            </tr>

                            <tr class="even">
                                <td align="center">
	                                Cache Providers
                                </td>
                                <td align="left">
                                    <select name="<%=GlobalConstants.EDIT_FORM_CACHE_PROVIDER%>" class="risposta">
                                        <%
                                            CacheProvider currentCache = null;
                                            Iterator<CacheProvider> cacheIterator = cacheProviders.iterator();
                                            while (cacheIterator.hasNext()) {
                                                currentCache = cacheIterator.next();
                                                if (currentCache.getClass().getName().equals(currentFolder.getCacheProvider())) {
                                                %>
                                                    <option value="<%=currentCache.getClass().getName()%>" selected="true"><%=currentCache.getName()%></option>
                                                <%
                                                } else {
                                                %>
                                                    <option value="<%=currentCache.getClass().getName()%>"><%=currentCache.getName()%></option>
                                                <%
                                                }
                                            }
                                        %>                                        
                                    </select>
                                </td>
                                <td align="left">
                                    <ul>
                                        <%
                                            cacheIterator = cacheProviders.iterator();
                                            while (cacheIterator.hasNext()) {
                                                currentCache = cacheIterator.next();
                                        %>
                                        <li>
                                            <b><%=currentCache.getName()%>:</b> <%=currentCache.getHtmlDescription()%>
                                        </li><br/>
                                        <%  }  %>
                                    </ul>
                                </td>
                            </tr>

                            <tr class="odd">
                                <td align="center">
	                                Retrivers
                                </td>
                                <td align="left">
                                    <select name="<%=GlobalConstants.EDIT_FORM_RETRIVER%>" class="risposta">
                                        <%
                                            currentHandler = null;
                                            handlerIterator = retrivers.iterator();
                                            while (handlerIterator.hasNext()) {
                                                currentHandler = handlerIterator.next();
                                                if (currentHandler.getClass().getName().equals(currentFolder.getRetriver())) {
                                                %>
                                                    <option value="<%=currentHandler.getClass().getName()%>" selected="true"><%=currentHandler.getName()%></option>
                                                <%
                                                } else {
                                                %>
                                                    <option value="<%=currentHandler.getClass().getName()%>"><%=currentHandler.getName()%></option>
                                                <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                                <td align="left">
                                    <ul>
                                        <%
                                            handlerIterator = retrivers.iterator();
                                            while (handlerIterator.hasNext()) {
                                                currentHandler = handlerIterator.next();
                                        %>
                                        <li>
                                            <b><%=currentHandler.getName()%>:</b> <%=currentHandler.getHtmlDescription()%>
                                        </li><br/>
                                        <%  }  %>
                                    </ul>
                                </td>
                            </tr>

                            <tr class="even">
                                <td align="center">
	                                Transformers
                                </td>
                                <td align="left">
                                    <%
                                        currentHandler = null;
                                        handlerIterator = transformers.iterator();
                                        while (handlerIterator.hasNext()) {
                                            currentHandler = handlerIterator.next();
                                            boolean found=false;
                                            selectedHandlers = currentFolder.getTransformers().iterator();
                                            while(selectedHandlers.hasNext())
                                                if (currentHandler.getClass().getName().equals(selectedHandlers.next()))
                                                    found=true;

                                            if (found) {
                                            %>
                                                <div class="checkbox"><input type="checkbox" name="<%=GlobalConstants.EDIT_FORM_TRANSFORMERS%>" value="<%=currentHandler.getClass().getName()%>" checked="true"/><%=currentHandler.getName()%></div>
                                            <%
                                            } else {
                                            %>
                                                <div class="checkbox"><input type="checkbox" name="<%=GlobalConstants.EDIT_FORM_TRANSFORMERS%>" value="<%=currentHandler.getClass().getName()%>"/><%=currentHandler.getName()%></div>
                                            <%
                                            }
                                        }
                                    %>
                                </td>
                                <td align="left">
                                    <ul>
                                        <%
                                            handlerIterator = transformers.iterator();
                                            while (handlerIterator.hasNext()) {
                                                currentHandler = handlerIterator.next();
                                        %>
                                        <li>
                                            <b><%=currentHandler.getName()%>:</b> <%=currentHandler.getHtmlDescription()%>
                                        </li><br/>
                                        <%  }  %>
                                    </ul>
                                </td>
                            </tr>

                            <tr class="odd">
                                <td align="center">
	                                Serializers
                                </td>
                                <td align="left">
                                    <select name="<%=GlobalConstants.EDIT_FORM_SERIALIZER%>" class="risposta">
                                        <%
                                            currentHandler = null;
                                            handlerIterator = serializers.iterator();
                                            while (handlerIterator.hasNext()) {
                                                currentHandler = handlerIterator.next();
                                                if (currentHandler.getClass().getName().equals(currentFolder.getSerializer())) {
                                                %>
                                                    <option value="<%=currentHandler.getClass().getName()%>" selected="true"><%=currentHandler.getName()%></option>
                                                <%
                                                } else {
                                                %>
                                                    <option value="<%=currentHandler.getClass().getName()%>"><%=currentHandler.getName()%></option>
                                                <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                                <td align="left">
                                    <ul>
                                        <%
                                            handlerIterator = serializers.iterator();
                                            while (handlerIterator.hasNext()) {
                                                currentHandler = handlerIterator.next();
                                        %>
                                        <li>
                                            <b><%=currentHandler.getName()%>:</b> <%=currentHandler.getHtmlDescription()%>
                                        </li><br/>
                                        <%  }  %>
                                    </ul>
                                </td>
                            </tr>

                        </table>
                    </div>
                    <div id="line"></div>
                    <table width="95%" align="center">
                        <tr class="submit">
                            <td width="100%" align="center" >
                                <input type="submit" value="Apply Changes" />
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
        <div id="fondo">
            <div id="author">
                    <p><b><%=context.getProxymaVersion()%></b> - Marco Casavecchia Morganti (marcolinuz@gmail.com)</p>
            </div>
            <div id="www">
                    <a href="http://proxyma.sourceforge.net/">Proxyma Project</a>
            </div>
            <div id="release">
                <img src="<%=proxymaContextPath%>/img/angolo_grigio_rev.gif" alt="grey angle image" />
            </div>
        </div>
    </body>
</html>

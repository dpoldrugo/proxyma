<%@page contentType="text/html" pageEncoding="UTF-8"
         import="m.c.m.proxyma.context.ProxyFolderBean,
                 m.c.m.proxyma.ProxymaTags,
                 m.c.m.proxyma.context.ProxymaContext,
                 m.c.m.proxyma.GlobalConstants,
                 java.util.Collection,
                 java.util.Iterator" %>
<!--
    Document   : contextOverview.jsp
    Description:
       This is the overvirw page for a specific context

    NOTE:
       this software is released under GPL License.
       See the LICENSE of this distribution for more informations.

       @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
       @version $Id: contextOverview.jsp 169 2010-07-02 21:11:21Z marcolinuz $
-->
<html>
     <%
         String proxymaContextPath = getServletContext().getContextPath();
         ProxymaContext context = (ProxymaContext)request.getAttribute(GlobalConstants.CONTEXT_REQUEST_ATTRIBUTE);
         Collection proxyFoldersCollection = (Collection<ProxyFolderBean>)request.getAttribute(GlobalConstants.PROXY_FOLDER_LIST_REQUEST_ATTRIBUTE);
     %>
    <head>
        <title>Proxyma Configuration Console</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link href="<%=proxymaContextPath%>/stile.css" rel="stylesheet" type="text/css">
    </head>
    <body>
    <jsp:include page="/header.html" />
    <div id="path" class="percorso">
        You are Here: <a href="<%=proxymaContextPath%>">Context Selection</a> -> <a href="<%=proxymaContextPath%>/console?<%=GlobalConstants.COMMAND_PARAMETER%>=<%=GlobalConstants.RELOAD_PAGE_COMMAND%>&<%=GlobalConstants.TARGET_PARAMETER%>=null&<%=GlobalConstants.ACTION_PARAMETER%>=<%=GlobalConstants.RELOAD_OVERVIEW_ACTION%>&<%=GlobalConstants.CONTEXT_PARAMETER%>=<%=context.getName()%>"><%=context.getName()%> context overview</a>
    </div>
    <div id="centrale">
        <div id="titolo">Proxy-Folder Destinations.</div>
        <%
            String message = (String)request.getAttribute(GlobalConstants.USER_MESSAGE_REQUEST_ATTRIBUTE);
            if (message != null) {
        %>
           <div class="message">
                <font color="red"><b><center><%=message%></center></b></font>
           </div>
        <%
            }
        %>
        <br/>
        <div id="gruppo">
            <table width="98%" align="center" >
                <tr class="labels">
                    <td width="20%"><b>Proxy Folder</b></td>
                    <td width="50%"><b>Remote Resource URL</b></td>
                    <td align="center"  width="10%"><b>Status</b></td>
                    <td align="center"  width="10%"><b>Modify</b></td>
                    <td align="center"  width="10%"><b>Remove</b></td>
                </tr>
<%
    Iterator<ProxyFolderBean> iter = proxyFoldersCollection.iterator();
    boolean even = true;
    String rowClass=null;
    while (iter.hasNext()) {
        ProxyFolderBean proxyFolder = iter.next();
        if (even)
            rowClass="even";
        else
            rowClass="odd";
        even = !even;
%>
                        <tr class="<%=rowClass%>">
                            <td><a href="<%=context.getProxymaContextBasePath()%>/<%=proxyFolder.getURLEncodedFolderName()%>/"><%=proxyFolder.getFolderName()%></a></td>
                            <td><a href="<%=proxyFolder.getDestinationAsString()%>"><%=proxyFolder.getDestinationAsString()%></a></td>
                            <td align="center" >
                            <% if (proxyFolder.isEnabled()) { %>
                                    <a href="<%=proxymaContextPath%>/console?<%=GlobalConstants.COMMAND_PARAMETER%>=<%=GlobalConstants.MANAGE_FOLDER_COMMAND%>&<%=GlobalConstants.TARGET_PARAMETER%>=<%=proxyFolder.getURLEncodedFolderName()%>&<%=GlobalConstants.ACTION_PARAMETER%>=<%=GlobalConstants.DISABLE_FOLDER_ACTION%>&<%=GlobalConstants.CONTEXT_PARAMETER%>=<%=context.getName()%>"><img class="folderActions" alt="enabled" src="<%=proxymaContextPath%>/img/running.png" /></a>
                            <% } else { %>
                                    <a href="<%=proxymaContextPath%>/console?<%=GlobalConstants.COMMAND_PARAMETER%>=<%=GlobalConstants.MANAGE_FOLDER_COMMAND%>&<%=GlobalConstants.TARGET_PARAMETER%>=<%=proxyFolder.getURLEncodedFolderName()%>&<%=GlobalConstants.ACTION_PARAMETER%>=<%=GlobalConstants.ENABLE_FOLDER_ACTION%>&<%=GlobalConstants.CONTEXT_PARAMETER%>=<%=context.getName()%>"><img class="folderActions" alt="disabled" src="<%=proxymaContextPath%>/img/locked.png" /></a>
                            <% } %>
                            </td>
                            <td align="center">
                                    <a href="<%=proxymaContextPath%>/console?<%=GlobalConstants.COMMAND_PARAMETER%>=<%=GlobalConstants.MANAGE_FOLDER_COMMAND%>&<%=GlobalConstants.TARGET_PARAMETER%>=<%=proxyFolder.getURLEncodedFolderName()%>&<%=GlobalConstants.ACTION_PARAMETER%>=<%=GlobalConstants.GO_TO_EDIT_FOLDER_ACTION%>&<%=GlobalConstants.CONTEXT_PARAMETER%>=<%=context.getName()%>"><img class="folderActions" alt="Edit Rule" src="<%=proxymaContextPath%>/img/modify.png" /></a>
                            </td>
                            <td align="center">
                                    <a href="<%=proxymaContextPath%>/console?<%=GlobalConstants.COMMAND_PARAMETER%>=<%=GlobalConstants.MANAGE_FOLDER_COMMAND%>&<%=GlobalConstants.TARGET_PARAMETER%>=<%=proxyFolder.getURLEncodedFolderName()%>&<%=GlobalConstants.ACTION_PARAMETER%>=<%=GlobalConstants.DELETE_FOLDER_ACTION%>&<%=GlobalConstants.CONTEXT_PARAMETER%>=<%=context.getName()%>" onClick="return confirm('Are you sure you want to Remove this Proxy-Folder?');"><img class="folderActions" alt="Remove Rule"  src="<%=proxymaContextPath%>/img/remove.png" /></a>
                            </td>
                        </tr>
<%
    }
%>
            </table>
            <br>
            <div align="center" class="submit">
                <form action="<%=proxymaContextPath%>/console" method="post">
                    <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER%>" value="<%=GlobalConstants.ADD_NEW_FOLDER_COMMAND%>" />
                    <input type="hidden" name="<%=GlobalConstants.CONTEXT_PARAMETER%>" value="<%=context.getName()%>" />
                    <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER%>" value="null" />
                    <input type="hidden" name="<%=GlobalConstants.ACTION_PARAMETER%>" value="null" />
                    <input type="submit" value="Create new Proxy-Folder"/>
                </form>
            </div>
            <div align="center" class="endlinks" >
                <table  width="98%">
                    <tr>
                        <td width="33%" align="right">
                            <form action="<%=proxymaContextPath%>/console" method="post">
                                <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER%>" value="<%=GlobalConstants.IMPORT_CONTEXT_COMMAND%>" />
                                <input type="hidden" name="<%=GlobalConstants.CONTEXT_PARAMETER%>" value="<%=context.getName()%>" />
                                <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER%>" value="null" />
                                <input type="hidden" name="<%=GlobalConstants.ACTION_PARAMETER%>" value="null" />
                                <input type="submit" value="Import Proxy-Folders"/>
                            </form>
                        </td>
                        <td width="34%">
                            <form action="<%=proxymaContextPath%>/console" method="post">
                                <span class="loglevel">Log level:</span>
                                <select onchange="form.submit()" class="logdropdown" name="<%=GlobalConstants.ACTION_PARAMETER%>">
                                    <%
                                        ProxymaTags.LogLevels levels[] =  ProxymaTags.LogLevels.values();
                                        for(int count=0; count < levels.length; count++) {
                                            if (levels[count].toString().equals(context.getLogLevel())) {
                                    %>
                                                <option value="<%=levels[count].toString()%>" selected="true"><%=levels[count].toString()%></option>
                                    <%
                                            } else {
                                    %>
                                                <option value="<%=levels[count].toString()%>"><%=levels[count].toString()%></option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                                <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER%>" value="<%=GlobalConstants.CHANGE_LOG_LEVEL_COMMAND%>" />
                                <input type="hidden" name="<%=GlobalConstants.CONTEXT_PARAMETER%>" value="<%=context.getName()%>" />
                                <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER%>" value="null" />
                            </form>
                        </td>
                        <td width="33%" align="left">
                            <form action="<%=proxymaContextPath%>/console" method="post">
                                <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER%>" value="<%=GlobalConstants.EXPORT_CONTEXT_COMMAND%>" />
                                <input type="hidden" name="<%=GlobalConstants.CONTEXT_PARAMETER%>" value="<%=context.getName()%>" />
                                <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER%>" value="null" />
                                <input type="hidden" name="<%=GlobalConstants.ACTION_PARAMETER%>" value="null" />
                                <input type="submit" value="Export Proxy-Folders"/>
                            </form>
                        </td>
                    </tr>
                </table>
            </div>
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


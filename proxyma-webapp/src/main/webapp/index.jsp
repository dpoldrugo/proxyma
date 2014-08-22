<%@page contentType="text/html" pageEncoding="UTF-8"
        import="m.c.m.proxyma.ProxymaFacade, 
                m.c.m.proxyma.GlobalConstants,
                java.util.Enumeration"%>
<!--
    Document   : index.jsp
    Description:
       This is the welcome page of the proxyma-console.

    NOTE:
       this software is released under GPL License.
       See the LICENSE of this distribution for more informations.

       @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
       @version $Id: index.jsp 169 2010-07-02 21:11:21Z marcolinuz $
-->
<html>
    <head>
        <title>Welcome to the new Proxyma-NG!</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link href="stile.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <jsp:include page="header.html" />
        <div id="centrale">
            <div id="titolo">Welcome to the new Proxyma-NG!</div>
            <div id="gruppo">
                <br/>
                    This is the configuration console of the application that implements the multiple<br/>
                    reverse-proxy with basic url-rewriting capabilities using the new proxyma-core library..
                <br/><br/>
                    Now, all you have to do is to select a context and click the "go" button to start to manage the proxy-folders.
                <br/><br/>
                <div id="line"></div>
                <span class="form">
                    <form action="console" method="post">
                        <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER %>" value="<%=GlobalConstants.RELOAD_PAGE_COMMAND %>"/>
                        <input type="hidden" name="<%=GlobalConstants.ACTION_PARAMETER %>" value="<%=GlobalConstants.RELOAD_OVERVIEW_ACTION %>"/>
                        <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER %>" value="none"/>
                        <select class="risposta" name="<%=GlobalConstants.CONTEXT_PARAMETER%>">
                            <%
                            ProxymaFacade proxyma = new ProxymaFacade();
                            Enumeration<String> contextNames = proxyma.getRegisteredContextNames();
                            while (contextNames.hasMoreElements()) {
                                String contextName = contextNames.nextElement();
                            %>
                            <option value="<%=contextName%>"><%=contextName%></option>
                            <%
                            }
                            %>
                        </select>
                        <input type="submit" value="Go!" />
                    </form>
                </span>
            </div>
        </div>
        <div id="fondo">
            <div id="author">
                    <p><b>Proxyma-NG</b> - By MCM (marcolinuz@gmail.com)</p>
            </div>
            <div id="www">
                    <a href="http://proxyma.sourceforge.net/">Proxyma Project</a>
            </div>
            <div id="release">
                <img src="<%=getServletContext().getContextPath()%>/img/angolo_grigio_rev.gif" alt="grey angle image" />
            </div>
        </div>
    </body>
</html>

package m.c.m.proxyma.plugins.transformers;

import java.net.URL;
import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import m.c.m.proxyma.rewrite.URLRewriteEngine;

/**
 * <p>
 * This Transformer processes the redirect responses.<br/>
 * Its goal is to change the "redirected location" if there is any proxy-folder
 * that masquerades it.<br/>
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: HttpRedirectsRewriteTransformer.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class HttpRedirectsRewriteTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public HttpRedirectsRewriteTransformer(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
        this.rewriter = new URLRewriteEngine(context);
    }

    /**
     * It scans the HTML page contained into the response searching for any URL.<br/>
     * When it finds an URL relative to the path of the current configured proxy folders,
     * it uses the UrlRewriterEngine to modify the URL.<br/>
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception {
        ProxymaResponseDataBean originalResponse = aResource.getResponse().getResponseData();

        //try to rewrite only the "rewritable" redirects.
        switch (originalResponse.getStatus()) {
            case MULTIPLE_CHOICES_REDIRECT:
            case MOVED_PERMANENTLY_REDIRECT:
            case FOUND_REDIRECT:
            case USE_PROXY_REDIRECT:
            case TEMPORARY_REDIRECT:
                ProxymaHttpHeader location = originalResponse.getHeader(LOCATION_HEADER);
                if (location != null) {
                    log.fine("Location url found, target: " + location.getValue());
                    String newLocation = rewriter.masqueradeURL(location.getValue(), aResource);
                    if (newLocation.startsWith("/")) {
                        URL proxymaRootURL = aResource.getProxymaRootURL();
                        StringBuffer proxymaHost = new StringBuffer(location.getValue().length());
                        proxymaHost.append(proxymaRootURL.getProtocol()).append("://");
                        proxymaHost.append(proxymaRootURL.getHost());
                        if (proxymaRootURL.getPort() > 0)
                            proxymaHost.append(":").append(proxymaRootURL.getPort());
                        proxymaHost.append(newLocation);
                        newLocation = proxymaHost.toString();
                        location.setValue(newLocation);
                        log.finer("Rewritten location target: " + newLocation);
                    } else {
                        log.finer("Redirect to an external target not rewritten.");
                    }
                }
                break;
        }
    }

    /**
     * Returns the name of the plugin.
     * @return the name of the plugin.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of what the plugin does..<br/>
     * You can use html tags into it.<br/>
     * The result of this method call can be used by any interface
     * to explain for what is the puropse of the plugin.
     *
     * @return a short description of the plugin
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }
    /**
     * The logger of the context..
     */
    private Logger log = null;
    /**
     * The rewriter engine capable to rewrite URLs and Cookies.
     */
    private URLRewriteEngine rewriter = null;

    /**
     * The name of this plugin.
     */
    private static final String name = "Http Redirects Rewriter";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin rewrites redirected resources.<br/>"
            + "If the remote server response is a redirect (Status code 300, 302.. ), "
            + "this plugin will try to rewrite it in order to force the client "
            + "browser to use proxyma to fetch it.";

    /**
     * The standard Location header name.
     */
    private static final String LOCATION_HEADER = "Location";

    //status codes to ispect for location rewrite.
    private static final int MULTIPLE_CHOICES_REDIRECT = 300;
    private static final int MOVED_PERMANENTLY_REDIRECT = 301;
    private static final int FOUND_REDIRECT = 302;
    private static final int USE_PROXY_REDIRECT = 305;
    private static final int TEMPORARY_REDIRECT = 307;
}

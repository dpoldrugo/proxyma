package m.c.m.proxyma.plugins.preprocessors;

import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.plugins.serializers.PerformanceTestSerializer;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.rewrite.CookieRewriteEngine;

/**
 * <p>
 * This preprocessor recognizes the cookies that were modified by the
 * CookiesRewriteTransformer and restores the original values on them. <br/>
 * As just said, it works in conjunction with the CookiesRewriteTransformer and
 * it would be useless to activate it without its companion plugin.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @see CookiesRewriteTransformer
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: CookiesRewritePreprocessor.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class CookiesRewritePreprocessor extends m.c.m.proxyma.plugins.preprocessors.AbstractPreprocessor {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public CookiesRewritePreprocessor(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();

        //Load the cookies rewriter engine
        this.rewriter = new CookieRewriteEngine(context);
    }

    /**
     * This adds a resource attribute to the current resource in order to
     * memorize the instant when the new request has come to the server.
     * It can be used to verfy the performances of the server.
     * @param aResource any ProxymaResource
     * @see PerformanceTestSerializer
     */
    @Override
    public void process(ProxymaResource aResource) {
        ProxymaRequest theRequest = aResource.getRequest();
        Cookie[] requestCookies = theRequest.getCookies();

        if (requestCookies != null) {
            //Rewrites the cookie only if it was originally processed by the CookiesRewriteTransformer
            String cookieValue = null;
            for (int current = 0; current < requestCookies.length; current++) {
                cookieValue = requestCookies[current].getValue();
                if ((cookieValue != null) && (cookieValue.startsWith(CookieRewriteEngine.PROXYMA_REWRITTEN_HEADER))) {
                    //This is a rewritten cookie
                    rewriter.unmasqueradeCookie(requestCookies[current]);
                } else {
                    //This is a local cookie not to forwrad!
                    //Overwrite it with non-sense value to protect your privacy
                    requestCookies[current].setValue("To protect your privacy, the Proxyma-NG CookiesRewritePreprocessor has ereased this Cookie.");
                }
            }
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
     * The cookie rewriter used by this plugin
     */
    private CookieRewriteEngine rewriter = null;
    /**
     * The name of this plugin.
     */
    private static final String name = "Cookies Rewrite Preprocessor";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This Preprocessor recognizes the rewritten cookies and restore them with their original values.<br/>"
            + "It works togheter the <i>CookiesRewriteTransformer</i> plugin. So it's useless to activate it without its companion.";
}

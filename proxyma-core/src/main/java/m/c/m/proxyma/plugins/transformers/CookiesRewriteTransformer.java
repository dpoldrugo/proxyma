package m.c.m.proxyma.plugins.transformers;

import java.util.Iterator;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.plugins.preprocessors.CookiesRewritePreprocessor;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import m.c.m.proxyma.rewrite.CookieRewriteEngine;

/**
 * <p>
 * This Transformer processes all the cookies fuonded into the response.<br/>
 * Its goal is to change domain and path of the cookies to allow client
 * browsers to manage them properly.<br/>
 * This plugin works in conjunction with the CookiesRewritePreprocessor to
 * restores the original values for the masqueraded hosts, so
 * it would be useless to activate it without its companion plugin.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @see CookiesRewritePreprocessor
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: CookiesRewriteTransformer.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class CookiesRewriteTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public CookiesRewriteTransformer(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
        this.rewriter = new CookieRewriteEngine(context);
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

        Iterator<Cookie> responseCookies = originalResponse.getCookies().iterator();
        Cookie currentCookie = null;
        while (responseCookies.hasNext()) {
            currentCookie = responseCookies.next();
            log.fine("Rewriting Cookie: " + currentCookie.getName());
            rewriter.masqueradeCookie(currentCookie, aResource);
            log.finer("Rewritten cookie, original domain/path: " + currentCookie.getComment());
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
    private CookieRewriteEngine rewriter = null;

    /**
     * The name of this plugin.
     */
    private static final String name = "Cookies Rewriter";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is a Cookie Transformer.<br/>"
            + "It manipulates all the transiting cookies by changing them in order to allow the client "
            + "browser to transparently access the remote server through proxyma.<br/>"
            + "Note: this plugin works togheter the CookiesRewritePreprocessor "
            + "(that instead restores the original values of the masqueraded cookies), "
            + "for this reason, it would be useless to activate it without its companion.";
}

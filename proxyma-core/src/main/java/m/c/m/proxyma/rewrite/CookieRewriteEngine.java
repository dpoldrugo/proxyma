package m.c.m.proxyma.rewrite;

import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This Class implements the logic of the Cookies rewriter engine.<br/>
 * It is used by the plugins that performs Cookie rewriting stuff.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: CookieRewriteEngine.java 176 2010-07-03 09:02:14Z marcolinuz $
 */
public class CookieRewriteEngine {
    public CookieRewriteEngine (ProxymaContext context) {
        //initialize the logger for this class.
        log = context.getLogger();
        urlRewriter = new URLRewriteEngine(context);
    }

    /**
     * Masquerade to the client a cookie that comes froma a remote host by
     * setting its domain to the domain of proxyma and the path to the path
     * of the  current proxy-folder.
     *
     * @param cookie the cookie to masquerade
     * @param aResource the resource that owns the Cookie
     */
    public void masqueradeCookie(Cookie cookie, ProxymaResource aResource) {
        //calculate the new values of the Set-Cookie header
        URL proxymaRootURL = aResource.getProxymaRootURL();

        //Calculate the new Cookie Domain
        cookie.setDomain(proxymaRootURL.getHost());
        
        // calculate new path of the cookie
        if (cookie.getPath() == null) {
            cookie.setPath(urlRewriter.masqueradeURL(aResource.getProxyFolder().getDestinationAsURL().getPath(), aResource));
        } else {
            String newPath = urlRewriter.masqueradeURL(cookie.getPath(), aResource);
            if (newPath.startsWith("/")) {
                cookie.setPath(newPath);
            } else {
                cookie.setPath(urlRewriter.masqueradeURL(aResource.getProxyFolder().getDestinationAsURL().getPath(), aResource));
            }
        }

        //set the new value for the cookie
        String newValue = PROXYMA_REWRITTEN_HEADER + cookie.getValue();
        cookie.setValue(newValue);

        log.finer("Masqueraded Cookie, new path=" + cookie.getPath() + "; new value=" + newValue);
    }

    /**
     * Rebuilds the original cookie from a masqueraded one.
     * @param cookie the cookie to unmasquerade
     * @return an string array with doamain, path and original value of the cookie.
     */
    public void unmasqueradeCookie (Cookie cookie) {
        String cookieValue = cookie.getValue();
        if (cookieValue.startsWith(PROXYMA_REWRITTEN_HEADER)) {
            String originalValue = cookieValue.substring(33);
            cookie.setValue(originalValue);
            log.finer("Unmasqueraded Cookie original value: " + originalValue);
        }
    }

    /**
     * The logger for this class
     */
    private Logger log = null;

    /**
     * The url rewriter used to rewrite cookie paths
     */
    private URLRewriteEngine urlRewriter = null;

    /**
     * The header added to the rewritten cookies that can be recognized by the
     * preprocessor to restore the original values.
     */
    public static final String PROXYMA_REWRITTEN_HEADER = "#%#PROXYMA-NG_REWRITTEN_COOKIE#%#";
}

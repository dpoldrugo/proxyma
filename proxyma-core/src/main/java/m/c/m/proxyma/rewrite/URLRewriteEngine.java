package m.c.m.proxyma.rewrite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This Class implements the logic od the URL rewriter engine.<br/>
 * It is used by the plugins that performs URL rewriting.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: URLRewriteEngine.java 176 2010-07-03 09:02:14Z marcolinuz $
 */
public class URLRewriteEngine {
    public URLRewriteEngine (ProxymaContext context) {
        //initialize the logger for this class.
        log = context.getLogger();
    }

    /**
     * This method rewrites the passed url if it siteMatches the path of the
     * specified proxy-folder.
     * If not, it tryes to match any other configured fproxy-folder to keep the
     * client into the reverse proxy if possible.
     *
     * @param theURLPath the URL that have to be rewritten
     * @param aResource the resource that is currently processed by the ProxyEngine.
     * @return the rewritten URL.
     */
    public String masqueradeURL (String theURL, ProxymaResource aResource) {
        String retVal = null;
        //guess if it'an absolute or relative URL
        Matcher matcher = netAbsouleURLPattern.matcher(theURL);
        if (matcher.matches()) {
            //This is an absolute URL with schema://host[:potr]/and/path
            retVal = rewriteNetAbsoluteURL(theURL, aResource);
            log.finer("Rewritten net absolute URL: " + theURL + " -> " + retVal);
        } else {
            matcher = siteAbsoulteURLPattern.matcher(theURL);
            if(matcher.matches()) {
                //This is a site absolute URL
                retVal = rewriteSiteAbsoluteURL(theURL, aResource.getProxyFolder(), aResource.getProxymaRootURL().getPath());
                 log.finer("Rewritten site absolute URL: " + theURL + " -> " + retVal);
            } else {
                //This is a relative URL no rewriting is needed..
                retVal = theURL;
                log.finer("Relative URL: " + theURL + " not rewritten");
            }
        }

        return retVal;
    }

    /**
     * This method rewrites the full URLs<br/>
     * In other words it parses and rewrites complete URLS like
     * "http://site.host[:port]/path/to/resource.ext"
     *
     * @param theURLPath the URL that have to be rewritten
     * @param aResource the resource that is currently processed by the ProxyEngine.
     * @return the rewritten URL.
     */
    private String  rewriteNetAbsoluteURL(String theURL, ProxymaResource aResource) {
        String retVal = theURL;
        try {
            //Transform the URLs into jav.net.URL
            URL proxymaRootURL = aResource.getProxymaRootURL();
            URL urlToRewrite = new URL(theURL);
            StringBuffer pathToRewrite = new StringBuffer(theURL.length());
            pathToRewrite.append(urlToRewrite.getPath());
            if (urlToRewrite.getQuery() != null)
                pathToRewrite.append("?").append(urlToRewrite.getQuery());

            //Check if the schema siteMatches
            boolean siteMatches = true;
            if (!proxymaRootURL.getProtocol().equals(urlToRewrite.getProtocol()))
                siteMatches = false;

            //Check if the host name siteMatches
            if (siteMatches && !(proxymaRootURL.getHost().equals(urlToRewrite.getHost())))
                siteMatches = false;

            //guess ports for both urls
            int proxymaRootPort = URLUtils.getPort(proxymaRootURL);
            int urlToRewritePort = URLUtils.getPort(urlToRewrite);
            if (siteMatches && !(proxymaRootPort == urlToRewritePort))
                siteMatches = false;

            if (siteMatches) {
                //Rewrites the url using the site relative method
                retVal = rewriteSiteAbsoluteURL(pathToRewrite.toString(), aResource.getProxyFolder(), proxymaRootURL.getPath());
            } else {
                //Searches into the context for a matching destination
                ProxyFolderBean matchingFolder = searchMatchingProxyFolderIntoContext(urlToRewrite, aResource.getContext());

                //Rewrite the URL based upon the matched folder.
                if (matchingFolder != null)
                    retVal = rewriteSiteAbsoluteURL(pathToRewrite.toString(), matchingFolder, proxymaRootURL.getPath());
            }

        } catch (MalformedURLException ex) {
            log.fine("Malformed URL \"" + theURL + "\"not Rewritten");
        }
        return retVal;
    }

    /**
     * This method rewrites the urls that belongs to the site with an absoute path<br/>
     * In other words it parses and rewrites complete URLS like
     * "/path/to/resource.ext"
     *
     * @param theURLPath the URL that have to be rewritten
     * @param folder the proxy folder that "captured" the URL
     * @param context the current proxyma context.
     * @return the rewritten URL.
     */
    private String rewriteSiteAbsoluteURL(String theURLPath, ProxyFolderBean folder, String proxymaRootURLPath) {
        String retVal = null;

        //Get the path of the masqueraded destination path
        String masqueradedPath = folder.getDestinationAsURL().getPath();

        if (theURLPath.startsWith(masqueradedPath)) {
            //Get the Proxyma root path
            StringBuffer newPrefixURL = new StringBuffer(proxymaRootURLPath);

            //Add the proxyFolder to the proxyma path and obtaining the new prefix
            newPrefixURL.append("/").append(folder.getURLEncodedFolderName());

            //return the new url to the invoker.
            retVal = theURLPath.replaceFirst(masqueradedPath, newPrefixURL.toString());
        } else {
            //This is an absolute path external form the masqueraded path,
            //so the method have to return a net absolute link.
            URL destinationURL = folder.getDestinationAsURL();
            StringBuffer netAbsoluteDestination = new StringBuffer(destinationURL.getProtocol()).append("://");
            netAbsoluteDestination.append(destinationURL.getHost());
            if (destinationURL.getPort() > 0)
                netAbsoluteDestination.append(":").append(destinationURL.getPort());

            retVal = netAbsoluteDestination.append(theURLPath).toString();
        }
        return retVal;
    }

    /**
     * Search into the context for a proxy-folder that matches the passed URL.
     * @param theURLPath the url to search into the destinations
     * @param context the context to inspect
     * @return the matching proxy-folder if found.. else it returns null.
     */
    private ProxyFolderBean searchMatchingProxyFolderIntoContext(URL theURL, ProxymaContext context) {
        ProxyFolderBean retVal = null;
        String searchHost = URLUtils.getDestinationHost(theURL);
        Collection foundHosts = context.getProxyFolderByDestinationHost(searchHost);
        if (foundHosts != null) {
            Iterator<ProxyFolderBean> iterator = foundHosts.iterator();
            ProxyFolderBean folder = null;
            String URLPath = theURL.getPath();
            URL foundedDestination = null;
            boolean found = false;
            while (!found && iterator.hasNext()) {
                folder = iterator.next();
                foundedDestination = folder.getDestinationAsURL();
                if (URLPath.startsWith(foundedDestination.getPath())) {
                    retVal = folder;
                    found = true;
                }
            }
        }
        return retVal;
    }

    /**
     * The logger for this class
     */
    private Logger log = null;
    /**
     * This pattern siteMatches absolute URLS that can link anything on the net
     */
    private Pattern netAbsouleURLPattern = Pattern.compile("^.*://.*$");

    /**
     * This patterm siteMatches site absoute URLS that can link only resources
     * that belongs only to the masqueraded site.
     */
    private Pattern siteAbsoulteURLPattern = Pattern.compile("^/.*$");

    /**
     * The separator used into the cookie comment to store the original
     * domain and path fields.
     */
    private static final String COMMENT_FIELDS_SEPARATOR = "@";
}

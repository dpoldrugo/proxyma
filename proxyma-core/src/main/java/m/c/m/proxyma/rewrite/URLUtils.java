package m.c.m.proxyma.rewrite;

import java.net.URL;

/**
 * <p>
 * This class is a collection some useful function for the URL management
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: URLUtils.java 144 2010-06-23 22:24:28Z marcolinuz $
 */
public class URLUtils {
    /**
     * Translates the URL into a new URL countaining only the protocol,the host
     * and the port. (ex: http://www.a.b/c/d -> http://www.a.b:80)
     * @param theUrl
     * @return
     */
    public static String getDestinationHost(URL theUrl) {
        StringBuffer retVal = new StringBuffer(theUrl.getProtocol());
        retVal.append("://");
        retVal.append(theUrl.getHost());
        retVal.append(":");
        retVal.append(getPort(theUrl));
        return retVal.toString();
    }


    /**
     * Returns always the "real" port for the passed URL.<br/>
     * In other words it returns the port to use to make a connection to the
     * given url and never returns -1 unlike URL.getPort().
     * @param theUrl to check
     * @return the real port of the resource pointed by the url.
     */
    protected static int getPort(URL theUrl) {
        int retVal = 0;
        if (theUrl.getPort() == -1) {
            if ("http".equals(theUrl.getProtocol()))
                retVal = 80;
            else if ("https".equals(theUrl.getProtocol()))
                retVal = 443;
            else if ("ftp".equals(theUrl.getProtocol()))
                retVal = 21;
        } else {
            retVal = theUrl.getPort();
        }
        return retVal;
    }
}

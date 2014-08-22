package m.c.m.proxyma.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.context.ProxymaContext;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This the "FactoryMethod" to build the new instances of proxyma resources.
 * Currently it can build a ProxymaResource from HttpServlet request and response,
 * but it will be easy to add more "sources" like Portlet requests and responses.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaResourceFactory.java 152 2010-06-26 18:37:48Z marcolinuz $
 */
public class ProxymaResourceFactory {
    /**
     * This method creates a complete instance of ProxymaResource form a
     * servlet request, a servlet response and a proxyma context.
     * The produced resource can be handled by the reverse proxy engine and
     * by any of its registered plugins.
     *
     * @param request the servlet container request.
     * @param response the servlet container response
     * @param context the proxyma context where the resource will live.
     * @return an resource that can be directly handled by the ProxyEngine.
     * @throws NullArgumentException if any of the passed parameters is null
     */
    public ProxymaResource createNewResource(HttpServletRequest request, HttpServletResponse response, ProxymaContext context)
        throws NullArgumentException {
        ProxymaRequest proxymaRequest = new ProxymaServletRequest(request, context);
        ProxymaResponse proxymaResponse = new ProxymaServletResponse(response, context);
        return new ProxymaResource(proxymaRequest, proxymaResponse, context);
    }
}

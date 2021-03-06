package m.c.m.proxyma.plugins.retrivers;

import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaTags.HandlerType;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the abstract class of a retriver plugin.<br/>
 * you have to implement a subclass of this if you want to realize your own
 * preprocessor :O)
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: AbstractRetriver.java 174 2010-07-03 08:27:20Z marcolinuz $
 */
public abstract class AbstractRetriver implements m.c.m.proxyma.core.ResourceHandler {

    /**
     * This method is required to declare the type of plugin that this class
     * implements and it's final. So you don't have to override it.
     * @return the type of this plugin: RETRIVER
     */
    @Override
    public final HandlerType getType() {
        return ProxymaTags.HandlerType.RETRIVER;
    }

    /**
     * This is the method to implement to realize the retriver logic.
     * It have to provide some response data for the further plugins.
     * @param aResource any ProxymaResource
     */
    @Override
    public abstract void process(ProxymaResource aResource) throws Exception;

    /**
     * Implement this method to return the name of the plugin.
     * @return the name of the plugin
     */
    @Override
    public abstract String getName();

    /**
     * Implement this method to provide a short description of what the plugin
     * does.. you can use html tags into it.
     * @return a short description of the plugin
     */
    @Override
    public abstract String getHtmlDescription();
}

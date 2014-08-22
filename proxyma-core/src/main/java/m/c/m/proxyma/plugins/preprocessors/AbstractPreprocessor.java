package m.c.m.proxyma.plugins.preprocessors;

import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaTags.HandlerType;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the abstract class of a preprocessor plugin.<br/>
 * you have to implement a subclass of this if you want to realize your own
 * preprocessor :O)
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: AbstractPreprocessor.java 175 2010-07-03 08:32:15Z marcolinuz $
 */
public abstract class AbstractPreprocessor implements m.c.m.proxyma.core.ResourceHandler {

    /**
     * This method is required to declare the type of plugin that this class
     * implements and it's final. So you don't have to override it.
     * @return the type of this plugin: PREPROCESSOR
     */
    @Override
    public final HandlerType getType() {
        return ProxymaTags.HandlerType.PREPROCESSOR;
    }

    /**
     * This is the method to implement to realize a preprocessor
     * @param aResource any ProxymaResource
     */
    @Override
    public abstract void process(ProxymaResource aResource);
    
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

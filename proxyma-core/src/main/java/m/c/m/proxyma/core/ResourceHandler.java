package m.c.m.proxyma.core;

import java.io.IOException;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This is the interface to implement Preprocessors, Transformers, Serializers
 * and Retrivers.
 * However, it's not recommanded to build plugins starting from this interface.
 * To do so, you should extend and override the "process" method of the "AbstraceHandlers".
 *
 * @see AbstractPreprocessord, AbstractSerializer, AbstractTransformer and AbstractRetriver
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ResourceHandler.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public interface ResourceHandler {

    /**
     * Return the the type of the Resource handler.
     * There are 4 types of resource handlers:
     * PREPROCESSOR, RETRIVER, TRANSFORMER and SERIALIZER.
     *
     * @return the value that rappresents the type of the handler.
     */
    public ProxymaTags.HandlerType getType();

    /**
     * Implements the business logic of the plugin.
     *
     * @param aResource the resource to process.
     */
    public void process(ProxymaResource aResource) throws Exception;

    /**
     * Returns the name of the plugin (only a name that characterize the plugin,
     * not the class name..) that will be used into the interfaces as plugin name.
     * @return the plugin name.
     */
    public String getName();

    /**
     * Returns a short description (html formatted) of the plugin.<br>
     * It will be used into the interfaces to give some information about the
     * plugin.
     *
     * @return a short description of the plugin.
     */
    public String getHtmlDescription();

}

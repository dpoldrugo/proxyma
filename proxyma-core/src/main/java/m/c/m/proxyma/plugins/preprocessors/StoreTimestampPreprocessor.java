package m.c.m.proxyma.plugins.preprocessors;

import java.util.Date;
import java.util.logging.Logger;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This preprocessor realize a component that registers into a resource attribute
 * the time when the request has come to the server. <br/>
 * It works in conjunction with the PerformanceTestSerializer and it could be
 * useful to test the performances of the server.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: StoreTimestampPreprocessor.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class StoreTimestampPreprocessor extends m.c.m.proxyma.plugins.preprocessors.AbstractPreprocessor {
    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public StoreTimestampPreprocessor (ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
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
        Date now = new  Date();
        aResource.addAttibute(timestampAttribute, now);
        log.fine("Added the timestamp to the resource attributes.");
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
     * This is the name of the attribute that will be set into the resource.
     */
    private static final String timestampAttribute = "Timestamp";

    /**
     * The name of this plugin.
     */
    private static final String name = "Store Timestamp Preprocessor";

    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
            "This plugin registers a timestamp into the incoming resources.<br/>" +
            "In other words it writes an attribute named \"Timestamp\" in the resource so " +
            "it can be used later by another plugin to check the performances of the proxy engine.<br/>" +
            "Of course, you don't really need it into a production environment.";
}

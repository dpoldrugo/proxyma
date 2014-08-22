package m.c.m.proxyma.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.plugins.caches.EhcacheCacheProvider;
import m.c.m.proxyma.plugins.caches.NullCacheProvider;
import m.c.m.proxyma.plugins.preprocessors.AbstractPreprocessor;
import m.c.m.proxyma.plugins.retrivers.AbstractRetriver;
import m.c.m.proxyma.plugins.serializers.AbstractSerializer;
import m.c.m.proxyma.plugins.transformers.AbstractTransformer;

/**
 * <p>
 * Test the functionality of the ProxyEngineFactory
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxyEngineFactoryTest.java 167 2010-06-30 23:40:44Z marcolinuz $
 */
public class ProxyEngineFactoryTest extends TestCase {
    
    public ProxyEngineFactoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of createNewProxyEngine method, of class ProxyEngineFactory.
     */
    public void testCreateNewProxyEngine() throws Exception {
        System.out.println("createNewProxyEngine");
        ProxyEngine instance = null;
        Collection plugins = null;
        Iterator iter = null;
        System.out.println("ProxyEngineFactory");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        try {
            instance = proxyma.createNewProxyEngine(context);
        } catch (IllegalAccessException ex) {
            context.getLogger().log(Level.SEVERE, null, ex);
        }

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredCachePlugins();
        assertEquals(2, plugins.size());
        iter=plugins.iterator();
        if (iter.next() instanceof  NullCacheProvider) {
           assertTrue(iter.next() instanceof  EhcacheCacheProvider);
        } else
            assertTrue(iter.next() instanceof  NullCacheProvider);


        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.PREPROCESSOR);
        assertEquals(2, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  AbstractPreprocessor);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.RETRIVER);
        assertEquals(2, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  AbstractRetriver);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.SERIALIZER);
        assertEquals(2, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  AbstractSerializer);

        //Test if the plugins are correctly loaded
        plugins = instance.getRegisteredPluginsByType(ProxymaTags.HandlerType.TRANSFORMER);
        assertEquals(6, plugins.size());
        iter=plugins.iterator();
        assertTrue(iter.next() instanceof  AbstractTransformer);

        //Test the value of the show folders on root uri flag
        assertTrue(instance.isEnableShowFoldersListOnRootURI());

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}

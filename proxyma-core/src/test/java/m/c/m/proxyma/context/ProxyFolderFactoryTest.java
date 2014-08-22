/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package m.c.m.proxyma.context;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaFacade;

/**
 * <p>
 * Test the functionality of the ProxyFolderFactory
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxyFolderFactoryTest.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class ProxyFolderFactoryTest extends TestCase {

    /**
     * Test of createNewProxyFolder method, of class ProxyFolderFactory.
     */
    public void testCreateNewProxyFolder() {
        System.out.println("createNewProxyFolder");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        
        assertEquals(expResult.getFolderName(), proxyFolderName);
        assertEquals(expResult.getDestinationAsString(), proxyFolderDestination);
        assertEquals(expResult.getMaxPostSize(), Integer.parseInt(context.getSingleValueParameter(ProxymaTags.FOLDER_MAX_POST_SIZE)));
        assertEquals(expResult.isEnabled(), false);
        assertEquals(expResult.getCacheProvider(), context.getSingleValueParameter(ProxymaTags.FOLDER_CACHEPROVIDER));
        assertEquals(expResult.getRetriver(), context.getSingleValueParameter(ProxymaTags.FOLDER_RETRIVER));
        assertEquals(expResult.getSerializer(), context.getSingleValueParameter(ProxymaTags.FOLDER_SERIALIZER));

        Collection <String> preprocessors = expResult.getPreprocessors();
        assertEquals(0, preprocessors.size());


        Iterator <String> transformers = expResult.getTransformers().iterator();
        int counter = 0;
        String expResults[] = new String[] {
                              "m.c.m.proxyma.plugins.transformers.CharsetInspectorTransformer",
                              "m.c.m.proxyma.plugins.transformers.CssUrlRewriteTransformer",
                              "m.c.m.proxyma.plugins.transformers.CookiesRewriteTransformer",
                              "m.c.m.proxyma.plugins.transformers.HttpRedirectRewriteTransformer",
                              "m.c.m.proxyma.plugins.transformers.HtmlUrlRewriteTransformer"};
        while (transformers.hasNext()) {
            assertEquals(transformers.next(), expResults[counter]);
            counter++;
        }
        assertEquals(5, counter);

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(expResult, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

}

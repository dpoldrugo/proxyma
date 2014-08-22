package m.c.m.proxyma.context;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaFacade;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * Test the functionality of the ProxyFolderBean
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxyFolderBeanTest.java 167 2010-06-30 23:40:44Z marcolinuz $
 */
public class ProxyFolderBeanTest extends TestCase {

    /**
     * Test of registerPreprocessor method, of class ProxyFolderBean.
     */
    public void testRegisterPreprocessor() {
        System.out.println("registerPreprocessor");
        String preprocessorClassName = "m.c.m.plugins.preprocessors.SomePreprocessor";
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.registerPreprocessor(null);
        Collection preprocessors = instance.getPreprocessors();
        assertEquals(0, preprocessors.size());

        instance.registerPreprocessor(preprocessorClassName);
        preprocessors = instance.getPreprocessors();
        assertEquals(1, preprocessors.size());

        instance.registerPreprocessor(" ");
        preprocessors = instance.getPreprocessors();
        assertEquals(1, preprocessors.size());

        instance.registerPreprocessor(" m.c.m.plugins.preprocessors.SomePreprocessor");
        preprocessors = instance.getPreprocessors();
        assertEquals(1, preprocessors.size());

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of unregisterPreprocessor method, of class ProxyFolderBean.
     */
    public void testUnregisterPreprocessor() {
        System.out.println("unregisterPreprocessor");
        String preprocessorClassName = "m.c.m.plugins.preprocessors.SomePreprocessor";
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.registerPreprocessor(preprocessorClassName);
        Collection preprocessors = instance.getPreprocessors();
        assertEquals(1, preprocessors.size());

        instance.unregisterPreprocessor(preprocessorClassName);
        preprocessors = instance.getPreprocessors();
        assertEquals(0, preprocessors.size());

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of registerTransformer method, of class ProxyFolderBean.
     */
    public void testRegisterTransformer() {
        System.out.println("registerTransformer");
        String transformerClassName = "m.c.m.plugins.transformers.SomeTransformer";
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.registerTransformer(null);
        Collection transformers = instance.getTransformers();
        assertEquals(5, transformers.size());

        instance.registerTransformer(transformerClassName);
        transformers = instance.getTransformers();
        assertEquals(6, transformers.size());

        instance.registerTransformer(" ");
        transformers = instance.getTransformers();
        assertEquals(6, transformers.size());

        instance.registerTransformer("m.c.m.plugins.transformers.SomeTransformer ");
        transformers = instance.getTransformers();
        assertEquals(6, transformers.size());

        Iterator<String> iter = instance.getTransformers().iterator();
        boolean found = false;
        while (iter.hasNext())
            if (transformerClassName.equals(iter.next()))
                found = true;
        assertTrue(found);

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of unregisterTransformer method, of class ProxyFolderBean.
     */
    public void testUnregisterTransformer() {
        System.out.println("unregisterTransformer");
        String transformerClassName = "m.c.m.core.SomeTransformer";
        String destination = "http://www.google.com";
        String folderName = "test";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.registerTransformer(transformerClassName);
        Collection transformers = instance.getTransformers();
        assertEquals(6, transformers.size());

        instance.unregisterTransformer(transformerClassName);
        transformers = instance.getTransformers();
        assertEquals(5, transformers.size());

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setFolderName method, of class ProxyFolderBean.
     */
    public void testSetFolderName() {
        System.out.println("setFolderName");
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        String folderName = "default";
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        try {
            folderName = null;
            instance.setFolderName(folderName);
            fail("Exception not thrown.");
        } catch (NullArgumentException x) {
           folderName = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            folderName = " ";
            instance.setFolderName(folderName);
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
           folderName = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            folderName = " amico/pippo";
            instance.setFolderName(folderName);
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
           folderName = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            folderName = " amico%20pippo ";
            String expected = "amico%20pippo";
            instance.setFolderName(folderName);
            assertEquals(instance.getFolderName(), expected);
        } catch (IllegalArgumentException x) {
           folderName = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            folderName = "default";
            instance.setFolderName(folderName);
        } catch (Exception x) {
            x.printStackTrace();
            fail("Exception thrown.");
        }
        assertEquals(instance.getFolderName(), folderName);

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getURLEncodedFolderName method, of class ProxyFolderBean.
     */
    public void testGetURLEncodedFolderName() {
        System.out.println("getURLEncodedFolderName");
        String destination = "http://www.google.com";
        String expected = null;
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        String folderName = "default";
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        try {
            folderName = " trying to fool ";
            expected = "trying+to+fool";
            instance.setFolderName(folderName);
        } catch (Exception x) {
            x.printStackTrace();
            fail("Exception thrown.");
        }
        assertEquals(instance.getURLEncodedFolderName(), expected);

        try {
            folderName = "now%2Ffake%2Fslashes";
            expected = "now%252Ffake%252Fslashes";
            instance.setFolderName(folderName);
        } catch (Exception x) {
            x.printStackTrace();
            fail("Exception thrown.");
        }
        assertEquals(instance.getURLEncodedFolderName(), expected);

        try {
            folderName = "default";
            instance.setFolderName(folderName);
        } catch (Exception x) {
            x.printStackTrace();
            fail("Exception thrown.");
        }
        assertEquals(instance.getFolderName(), folderName);

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setDestination method, of class ProxyFolderBean.
     */
    public void testSetDestination() {
        System.out.println("setDestination");
        String folderName = "test";
        String destination = "http://www.google.com";
        String expected = null;
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        try {
            destination = null;
            instance.setDestination(destination);
            fail("Exception not thrown.");
        } catch (NullArgumentException x) {
           destination = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            destination = " ";
            instance.setDestination(destination);
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
           destination = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            destination = "an.invalid.url";
            instance.setDestination(destination);
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
           destination = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }

        try {
            expected = "http://www.a.valid/destination";
            destination = " http://www.a.valid/destination ";
            instance.setDestination(destination);
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }
        assertEquals(instance.getDestinationAsString(), expected);
        assertNotNull(instance.getDestinationAsURL());
        

        try {
            expected = "http://www.a.valid/destination";
            destination = "http://www.a.valid/destination/";
            instance.setDestination(destination);
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception thrown");
        }
        assertEquals(instance.getDestinationAsString(), expected);
        assertNotNull(instance.getDestinationAsURL());

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setMaxPostSize method, of class ProxyFolderBean.
     */
    public void testSetMaxPostSize() {
        System.out.println("setMaxPostSize");
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.setMaxPostSize(-12);
        assertEquals(instance.getMaxPostSize(), ProxymaTags.UNSPECIFIED_POST_SIZE);

        instance.setMaxPostSize(12);
        assertEquals(instance.getMaxPostSize(), 12);

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setCacheProvider method, of class ProxyFolderBean.
     */
    public void testSetCacheProvider() {
        System.out.println("setCacheProvider");
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.setCacheProvider(null);
        assertEquals(instance.getCacheProvider(), ProxymaTags.UNSPECIFIED_CACHEPROVIDER);

        instance.setCacheProvider(" ");
        assertEquals(instance.getCacheProvider(), ProxymaTags.UNSPECIFIED_CACHEPROVIDER);

        instance.setCacheProvider(" m.c.m.proxyma.plugins.caches.SomeCachePRovider ");
        assertEquals(instance.getCacheProvider(), "m.c.m.proxyma.plugins.caches.SomeCachePRovider");

        instance.setCacheProvider("m.c.m.proxyma.plugins.caches.SomeCachePRovider");
        assertEquals(instance.getCacheProvider(), "m.c.m.proxyma.plugins.caches.SomeCachePRovider");

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setRetriver method, of class ProxyFolderBean.
     */
    public void testSetRetriver() {
        System.out.println("setRetriver");
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.setRetriver(null);
        assertEquals(instance.getRetriver(), ProxymaTags.UNSPECIFIED_RETRIVER);

        instance.setRetriver(" ");
        assertEquals(instance.getRetriver(), ProxymaTags.UNSPECIFIED_RETRIVER);

        instance.setRetriver(" m.c.m.proxyma.plugins.retrivers.SomeRetriver ");
        assertEquals(instance.getRetriver(), "m.c.m.proxyma.plugins.retrivers.SomeRetriver");

        instance.setCacheProvider("m.c.m.proxyma.plugins.retrivers.SomeRetriver");
        assertEquals(instance.getRetriver(), "m.c.m.proxyma.plugins.retrivers.SomeRetriver");

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setSerializer method, of class ProxyFolderBean.
     */
    public void testSetSerializer() {
        System.out.println("setSerializer");
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean instance = null;

        try {
            instance = proxyma.createNewProxyFolder(folderName, destination, context);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        instance.setSerializer(null);
        assertEquals(instance.getSerializer(), ProxymaTags.UNSPECIFIED_SERIALIZER);

        instance.setSerializer(" ");
        assertEquals(instance.getSerializer(), ProxymaTags.UNSPECIFIED_SERIALIZER);

        instance.setSerializer(" m.c.m.proxyma.plugins.serializers.SomeRetriver ");
        assertEquals(instance.getSerializer(), "m.c.m.proxyma.plugins.serializers.SomeRetriver");

        instance.setSerializer("m.c.m.proxyma.plugins.serializers.SomeRetriver");
        assertEquals(instance.getSerializer(), "m.c.m.proxyma.plugins.serializers.SomeRetriver");

        //Cleanup pool
        try {
            proxyma.removeProxyFolder(instance, context);
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}

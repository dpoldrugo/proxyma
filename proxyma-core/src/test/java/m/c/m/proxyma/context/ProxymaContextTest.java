package m.c.m.proxyma.context;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.ProxymaFacade;
import m.c.m.proxyma.rewrite.URLUtils;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * Test the functionality of the ProxymaContext
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaContextTest.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxymaContextTest extends TestCase {
    
    public ProxymaContextTest(String testName) {
        super(testName);
    }

    /**
     * Test of getProxyFolder method, of class ProxymaContext.
     */
    public void testGetProxyFolderByName() {
        System.out.println("getProxyFolderByName");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(expResult.getFolderName());
        assertSame(expResult, result);

        result = instance.getProxyFolderByURLEncodedName("notExists");
        assertNull(result);

        //clean up the context for further tests
        proxyma.removeProxyFolder(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getProxyFolder method, of class ProxymaContext.
     */
    public void testGetProxyFolderByDestinationHost() {
        System.out.println("getProxyFolderByDestinationHost");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com/default/it";
        String proxyFolderName2 = "default2";
        String proxyFolderDestination2 = "http://www.google.com/default/en";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;
        ProxyFolderBean expResult2 = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        Collection hostList = instance.getProxyFolderByDestinationHost(URLUtils.getDestinationHost(expResult.getDestinationAsURL()));
        assertEquals(1, hostList.size());
        assertEquals(hostList.iterator().next(), expResult);

        try {
            expResult2 = proxyma.createNewProxyFolder(proxyFolderName2, proxyFolderDestination2, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        hostList = instance.getProxyFolderByDestinationHost(URLUtils.getDestinationHost(expResult.getDestinationAsURL()));
        Iterator iterator = hostList.iterator();
        assertEquals(2, hostList.size());
        assertEquals(iterator.next(), expResult);
        assertEquals(iterator.next(), expResult2);
        

        //clean up the remove tests
        proxyma.removeProxyFolder(expResult, instance);

        hostList = instance.getProxyFolderByDestinationHost(URLUtils.getDestinationHost(expResult.getDestinationAsURL()));
        assertEquals(hostList.size(), 1);
        assertEquals(hostList.iterator().next(), expResult2);

        //clean up the remove tests
        proxyma.removeProxyFolder(expResult2, instance);

        hostList = instance.getProxyFolderByDestinationHost(URLUtils.getDestinationHost(expResult.getDestinationAsURL()));
        assertNull(hostList);


        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of addProxyFolder method, of class ProxymaContext.
     */
    public void testAddProxyFolder() {
        System.out.println("addProxyFolder");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        try {
            instance.addProxyFolder(null);
            fail("Exception not thrown");
        } catch (NullArgumentException x) {
            ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(null);
            assertNull(result);
        }

        //clean up the context because the folder is already registered
        proxyma.removeProxyFolder(expResult, instance);

        instance.addProxyFolder(expResult);
        ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(expResult.getFolderName());
        assertSame(expResult, result);

        try {
            instance.addProxyFolder(expResult);
            fail("Exception not thrown");
        } catch (IllegalArgumentException x) {
            int expResultCount = 1;
            int resultCount = instance.getProxyFoldersAsCollection().size();
            assertEquals(expResultCount, resultCount);   
        }

        //clean up the context for further tests
        proxyma.removeProxyFolder(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of removeProxyFolder method, of class ProxymaContext.
     */
    public void testRemoveProxyFolder() {
        System.out.println("removeProxyFolder");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        ProxyFolderBean result = instance.getProxyFolderByURLEncodedName(expResult.getFolderName());
        assertSame(expResult, result);

        int expResultCount = 1;
        int resultCount = instance.getProxyFoldersAsCollection().size();
        assertEquals(expResultCount, resultCount);

        try {
            instance.removeProxyFolder(null);
            fail("Exception not thrown");
        } catch (NullArgumentException x) {
            instance.removeProxyFolder(result);
            result = instance.getProxyFolderByURLEncodedName(proxyFolderName);
            assertNull(result);
        }

        try {
            instance.removeProxyFolder(result);
            fail("Exception not thrown");
        } catch (IllegalArgumentException x) {
            expResultCount = 0;
            resultCount = instance.getProxyFoldersAsCollection().size();
            assertEquals(expResultCount, resultCount);
        }

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of setLogLevel method, of class ProxyFolderBean.
     */
    public void testSetLogLevel() {
        System.out.println("setLogLevel");
        String folderName = "test";
        String destination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext context = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");

        context.setLogLevel("UNEXISTENT");
        assertEquals(ProxymaTags.UNSPECIFIED_LOGLEVEL, context.getLogLevel());

        context.setLogLevel("FINER");
        assertEquals("FINER",context.getLogLevel());

        //Cleanup pool
        try {
            proxyma.destroyContext(context);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of getProxyFoldersAsCollection method, of class ProxymaContext.
     */
    public void testGetProxyFoldersAsCollection() {
        System.out.println("getProxyFoldersAsCollection");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }

        int expResultCount = 1;
        int resultCount = instance.getProxyFoldersAsCollection().size();
        assertEquals(expResultCount, resultCount);

        Collection result = instance.getProxyFoldersAsCollection();
        assertTrue (result instanceof Collection);
        
        //clean up the context for further tests
        proxyma.removeProxyFolder(expResult, instance);
        
        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test the get getContextFoldersCount method
     */
    public void getProxyFoldersCount() {
        System.out.println("getProxyFoldersCount");
        String proxyFolderName = "default";
        String proxyFolderDestination = "http://www.google.com";
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");
        ProxyFolderBean expResult = null;

        try {
            expResult = proxyma.createNewProxyFolder(proxyFolderName, proxyFolderDestination, instance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ProxyFolderBean creation failed");
        }
        instance.addProxyFolder(expResult);

        int expResultCount = 1;
        assertEquals(expResultCount, instance.getProxyFoldersCount());

        //clean up the context for further tests
        proxyma.removeProxyFolder(expResult, instance);

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }

    /**
     * Test of loadConfiguration file and its methods.
     */
    public void testLoadAndGetConfigurationParameters() throws Exception {
        System.out.println("loadAndGetConfigurationParameters");
        ProxymaFacade proxyma = new ProxymaFacade();
        ProxymaContext instance = proxyma.createNewContext("default", "/", "src/test/resources/test-config.xml", "/tmp/");

        // Test Single value loading
        assertEquals("Single attribute load failed.", "single", instance.getSingleValueParameter("singleParameter"));

        // Test Attrivute value loading
        assertEquals("Attribute load failed", "attribute", instance.getSingleValueParameter("loadAttribute/@value"));

        // Test Multiple values loading
        Collection multiValueNames = instance.getMultiValueParameter("aggregation/multivalue/@name");
        Collection multiValueValues = instance.getMultiValueParameter("aggregation/multivalue");
        assertEquals("Number of multiple parameters wrong.", 3, multiValueNames.size());
        assertEquals("Number of multiple parameters wrong.", 3, multiValueValues.size());

        Iterator iterNames = multiValueNames.iterator();
        Iterator iterValues = multiValueValues.iterator();
        int counter = 0;
        while (iterNames.hasNext() && iterValues.hasNext()) {
            assertEquals((String)iterNames.next(), "name"+counter);
            assertEquals((String)iterValues.next(), "value"+counter);
            counter++;
        }

        //Cleanup pool
        try {
            proxyma.destroyContext(instance);
        } catch (Exception x) {
            fail("Unable to unregister the context");
        }
    }
}

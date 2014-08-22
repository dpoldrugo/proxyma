package m.c.m.proxyma.resource;

import junit.framework.TestCase;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * Test the functionality of the ProxymaHttpHeader
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaHttpHeaderTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class ProxymaHttpHeaderTest extends TestCase {

    /**
     * Test of getName method, of class ProxymaHttpHeader.
     */
    public void testConstructorAndGetName() {
        System.out.println("getName");
        ProxymaHttpHeader instance = null;
        String expResult = "";
        
        
        //Test constructor
        try {
            instance = new ProxymaHttpHeader(null, null);
            fail("Exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }
        
         //Test constructor
        try {
            instance = new ProxymaHttpHeader("  ", null);
            fail("Exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }
        
        //Test a normal header
        instance = new ProxymaHttpHeader("headername  ", null);
        expResult = "headername";
        assertEquals(expResult, instance.getName());
    }

    /**
     * Test of getValue method, of class ProxymaHttpHeader.
     */
    public void testGetValue() {
        System.out.println("getValue");
        ProxymaHttpHeader instance = null;
        String expResult = "";

        //Test constructor with null value
        expResult = "";
        instance = new ProxymaHttpHeader("  anHeader ", null);
        assertEquals(expResult, instance.getValue());

        expResult = "";
        instance = new ProxymaHttpHeader("  anHeader ", "    ");
        assertEquals(expResult, instance.getValue());

        expResult = "value";
        instance = new ProxymaHttpHeader("  anHeader ", " value ");
        assertEquals(expResult, instance.getValue());
    }

    /**
     * Test of setValue method, of class ProxymaHttpHeader.
     */
    public void testSetValue() {
        System.out.println("setValue");
        ProxymaHttpHeader instance = null;
        String expResult = "";

        //Test null value
        expResult = "oldvalue";
        instance = new ProxymaHttpHeader("anHeader", "oldvalue");
        assertEquals(expResult, instance.getValue());

        expResult = "";
        instance.setValue(null);
        assertEquals(expResult, instance.getValue());

        expResult = "newValue";
        instance.setValue(" newValue ");
        assertEquals(expResult, instance.getValue());
    }

    /**
     * Test of toString method, of class ProxymaHttpHeader.
     */
    public void testToString() {
        System.out.println("toString");
        ProxymaHttpHeader instance = null;
        String expResult = "name: value";

        //Test null value
        instance = new ProxymaHttpHeader("name", "value");
        assertEquals(expResult, instance.toString());
    }

    /**
     * Test of clone method, of class ProxymaHttpHeader.
     */
    public void testClone() throws CloneNotSupportedException {
        System.out.println("clone");

        ProxymaHttpHeader instance = new ProxymaHttpHeader("name", "value");
        ProxymaHttpHeader clone = (ProxymaHttpHeader) instance.clone();

        assertNotSame(instance, clone);
        assertEquals(instance.getName(), clone.getName());
        assertEquals(instance.getValue(), clone.getValue());
    }

}

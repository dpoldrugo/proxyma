package m.c.m.proxyma.resource;

import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.Cookie;
import junit.framework.TestCase;
import m.c.m.proxyma.buffers.RamBuffer;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * Test the functionality of the ProxymaResponseDataBean
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaResponseDataBeanTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class ProxymaResponseDataBeanTest extends TestCase {
    
    public ProxymaResponseDataBeanTest(String testName) {
        super(testName);
    }

    /**
     * Test of getHeaderNames method, of class ProxymaResponseDataBean.
     */
    public void testGetHeaderNames() {
        System.out.println("getHeaderNames");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("naMe1", "value1");
        instance.addHeader("Name1 ", "value1.1");
        instance.addHeader(" nAme2", "value2");
        Collection<String> result = instance.getHeaderNames();

        //test collection size
        assertEquals(2, result.size());

        //Test values
        Iterator<String> iter = result.iterator();
        if ("naMe1".equals(iter.next())) {
            assertEquals("nAme2", iter.next());
        } else {
            assertEquals("naMe1", iter.next());
        }

    }

    /**
     * Test of getMultipleHeaderValues method, of class ProxymaResponseDataBean.
     */
    public void testGetMultvalueHeader() {
        System.out.println("getMultivalueHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("namE1 ", "value1");
        instance.addHeader(" Name1", "value2");
        instance.addHeader("nAme3", "value3");
        Collection<ProxymaHttpHeader> result = instance.getMultivalueHeader("nAmE1");

        //Test size
        assertEquals(2, result.size());

        //Test multi values header
        Iterator<ProxymaHttpHeader> iter = result.iterator();
        if ("namE1: value1".equals(iter.next().toString())) {
            assertEquals("Name1: value2", iter.next().toString());
        } else {
            assertEquals("name1: value1", iter.next().toString());
        }

        //Test single value header
        result = instance.getMultivalueHeader("NAME3");
        assertEquals(1, result.size());
        iter = result.iterator();
        assertEquals("nAme3: value3", iter.next().toString());

        //Test unexisting header
        result = instance.getMultivalueHeader("Unexisting");
        assertNull(result);
    }

    /**
     * Test of getHeader method, of class ProxymaResponseDataBean.
     */
    public void testGetHeader() {
        System.out.println("getHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("Name1 ", "value1 ");
        instance.addHeader("naMe1", "value2");
        instance.addHeader(" Name3", " Value3");
        ProxymaHttpHeader result = instance.getHeader("nAmE1");
        assertEquals("Name1: value1", result.toString());

        result = instance.getHeader("name3");
        assertEquals("Name3: Value3", result.toString());

        result = instance.getHeader("Unexisting");
        assertNull(result);
    }

    /**
     * Test of addHeader method, of class ProxymaResponseDataBean.
     */
    public void testAddHeader() {
        System.out.println("addHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        
        try {
            instance.addHeader(null, null);
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
            assertTrue(true);
        }

        try {
            instance.addHeader("  ", "");
            fail("Exception not thrown.");
        } catch (IllegalArgumentException x) {
            assertTrue(true);
        }

        instance.addHeader("Name ", " Value");
        assertEquals("Name: Value", instance.getHeader("nAmE").toString());

        instance.addHeader("integerValue", 120);
        assertEquals("integerValue: 120", instance.getHeader("integervalue").toString());

        instance.addHeader("longValue", 12345678990L);
        assertEquals("longValue: 12345678990", instance.getHeader("Longvalue").toString());
    }

    /**
     * Test of containsHeader method, of class ProxymaResponseDataBean.
     */
    public void testContainsHeader() {
        System.out.println("containsHeader");

        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader(" name1 ", "value1");

        boolean result = instance.containsHeader("NaMe1");
        assertTrue(result);

        result = instance.containsHeader("Unexisting");
        assertFalse(result);
    }

    /**
     * Test of containsHeader method, of class ProxymaResponseDataBean.
     */
    public void isMultipleHeader() {
        System.out.println("isMultipleHeader");

        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader(" nAme1 ", "value1");
        instance.addHeader("naMe1 ", "value2");
        instance.addHeader("NamE3", " Value3");


        boolean result = instance.isMultipleHeader("NaMe1");
        assertTrue(result);

        result = instance.isMultipleHeader("Name3");
        assertFalse(result);

        try {
           result = instance.isMultipleHeader("Unexisting");
           fail("exception not thrown");
        } catch (NullPointerException x) {
            assertTrue(true);
        }

    }

    /**
     * Test of deleteHeader method, of class ProxymaResponseDataBean.
     */
    public void testDeleteHeader() {
        System.out.println("deleteHeader");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("nAme1 ", "value1");
        instance.addHeader("naMe1", "value2");
        instance.addHeader(" Name3 ", "Value3");

        Collection result = instance.getHeaderNames();
        assertEquals(2, result.size());

        instance.deleteHeader("nAmE3");
        result = instance.getHeaderNames();
        assertEquals(1, result.size());

        instance.deleteHeader("nAmE2");
        result = instance.getHeaderNames();
        assertEquals(1, result.size());

        instance.deleteHeader("NAme1");
        result = instance.getHeaderNames();
        assertEquals(0, result.size());
    }

    /**
     * Test of getCookies method, of class ProxymaResponseDataBean.
     */
    public void testGetCookies() {
        System.out.println("getCookies");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        Collection<Cookie> result = instance.getCookies();
        assertEquals(2, result.size());

        //Test multi values header
        Iterator<Cookie> iter = result.iterator();
        Cookie cookie = iter.next();
        if ("name1".equals(cookie.getName())) {
            assertEquals("value3", cookie.getValue());
            assertEquals("value2", iter.next().getValue());
        } else {
            assertEquals("name2", cookie.getName());
            assertEquals("value2", cookie.getValue());
            assertEquals("value3", iter.next().getValue());
        }

        instance = new ProxymaResponseDataBean();
        result = instance.getCookies();
        assertEquals(0, result.size());
    }

    /**
     * Test of getCookie method, of class ProxymaResponseDataBean.
     */
    public void testGetCookie() {
        System.out.println("getCookie");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        Cookie result = instance.getCookie("name1");
        assertEquals("value3", result.getValue());

        result = instance.getCookie("name2");
        assertEquals("value2", result.getValue());

        //Test unexisting value
        result = instance.getCookie("unexistent");
        assertNull(result);
    }

    /**
     * Test of addCookie method, of class ProxymaResponseDataBean.
     */
    public void testAddCookie() {
        System.out.println("addCookie");

        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();

        //Add null cookie
        try {
            instance.addCookie(null);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        //add empty cookie
        try {
            instance.addCookie(new Cookie(null, null));
            fail("exception not thrown");
        } catch (Exception x) {
            assertTrue(true);
        }

        instance.addCookie(new Cookie("name1", "value1"));
        assertEquals(1, instance.getCookies().size());
    }

    /**
     * Test of containsCookie method, of class ProxymaResponseDataBean.
     */
    public void testContainsCookie() {
        System.out.println("containsCookie");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        boolean result = instance.containsCookie("name1");
        assertTrue(result);

        result = instance.containsCookie("name2");
        assertTrue(result);

        //Test unexisting value
        result = instance.containsCookie("unexistent");
        assertFalse(result);

        //test null search
        result = instance.containsCookie(null);
        assertFalse(result);
    }

    /**
     * Test of deleteCookie method, of class ProxymaResponseDataBean.
     */
    public void testDeleteCookie() {
        System.out.println("deleteCookie");
        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addCookie(new Cookie("name1", "value1"));
        instance.addCookie(new Cookie("name2", "value2"));
        instance.addCookie(new Cookie("name1", "value3"));

        Collection<Cookie> result = instance.getCookies();
        assertEquals(2, result.size());

        try {
            instance.deleteCookie(null);
            fail("exception not thrown");
        } catch (NullArgumentException x) {
            assertTrue(true);
        }

        instance.deleteCookie("unexistent");
        result = instance.getCookies();
        assertEquals(2, result.size());

        instance.deleteCookie("name1");
        result = instance.getCookies();
        assertEquals(1, result.size());
    }

    /**
     * Test of clone method, of class ProxymaResponseDataBean.
     */
    public void testClone() throws CloneNotSupportedException {
        System.out.println("clone");

        ProxymaResponseDataBean instance = new ProxymaResponseDataBean();
        instance.addHeader("nAme1 ", "value1");
        instance.addCookie(new Cookie("name1", "value1"));
        instance.setData(new RamBuffer(1024));
        instance.setStatus(200);

        ProxymaResponseDataBean clone = (ProxymaResponseDataBean) instance.clone();

        assertNotSame(instance, clone);
        assertNotSame(instance.getCookie("name1"), clone.getCookie("name1"));
        assertNotSame(instance.getHeader("name1"), clone.getHeader("name1"));
        assertNotSame(instance.getData(), clone.getData());
        assertEquals(instance.getStatus(), clone.getStatus());
        assertEquals(instance.getCookie("name1").getValue(), clone.getCookie("name1").getValue());
        assertEquals(instance.getHeader("name1").getValue(), clone.getHeader("name1").getValue());
    }
}

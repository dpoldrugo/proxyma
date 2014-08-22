package m.c.m.proxyma.rewrite;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

/**
 * <p>
 * Test the functionality of the URLUtils
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: URLUtilsTest.java 144 2010-06-23 22:24:28Z marcolinuz $
 */
public class URLUtilsTest extends TestCase {
    
    public URLUtilsTest(String testName) {
        super(testName);
    }

    /**
     * Test of getDestinationHost method, of class URLUtils.
     */
    public void testGetDestinationHost() throws MalformedURLException {
        System.out.println("getDestinationHost");
        URL theUrl = new URL("http://pippo.pluto.com/a/c/d");
        String expResult = "http://pippo.pluto.com:80";
        String result = URLUtils.getDestinationHost(theUrl);
        assertEquals(expResult, result);

        theUrl = new URL("https://pippo.pluto.com/a/c/d");
        expResult = "https://pippo.pluto.com:443";
        result = URLUtils.getDestinationHost(theUrl);
        assertEquals(expResult, result);

        theUrl = new URL("https://pippo.pluto.com:8080/a/c/d");
        expResult = "https://pippo.pluto.com:8080";
        result = URLUtils.getDestinationHost(theUrl);
        assertEquals(expResult, result);
    }

}

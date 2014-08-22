package m.c.m.proxyma.resource;

import junit.framework.TestCase;

/**
 * <p>
 * Test the functionality of the ProxymaResponse
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxymaResponseTest.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class ProxymaResponseTest extends TestCase {
    
    public ProxymaResponseTest(String testName) {
        super(testName);
    }

    /**
     * Test of setResponseData method, of class ProxymaResponse.
     */
    public void testSetGetResponseData() {
        System.out.println("set/get ResponseData");
        ProxymaResponseDataBean responseData = new ProxymaResponseDataBean();
        ProxymaResponse instance = new ProxymaResponseImpl();
        
        //perform tests
        instance.setResponseData(responseData);
        assertSame(responseData, instance.getResponseData());

        instance.setResponseData(null);
        assertNull(instance.getResponseData());
    }
   
    /**
     * Test of hasBeenSent method, of class ProxymaResponse.
     */
    public void testSendingHasBeenSent() {
        System.out.println("sendingData/hasBeenSent");
        ProxymaResponse instance = new ProxymaResponseImpl();

        assertFalse(instance.hasBeenSent());

        instance.sendingData();
        assertTrue(instance.hasBeenSent());
    }

    public class ProxymaResponseImpl extends ProxymaResponse {

        public int sendDataToClient() throws IllegalStateException {
            return 0;
        }
    }

}

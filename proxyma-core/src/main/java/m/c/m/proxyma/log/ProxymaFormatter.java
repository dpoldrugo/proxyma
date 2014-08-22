package m.c.m.proxyma.log;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;

/**
 * <p>
 * This custom formatter introduce startup and shutdoen messages
 * to the standard simple formatter.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaFormatter.java 142 2010-06-21 21:42:16Z marcolinuz $
 */
public class ProxymaFormatter extends SimpleFormatter {

    /**
     * Initialize parent class.
     */
    public ProxymaFormatter () {
        super();
    }

    // This method is called just after the handler using this formatter is created
    @Override
    public String getHead(Handler h) {
        Date now = new Date();
        Format dateFormatter = new SimpleDateFormat(" [dd/MMM/yyyy:HH:mm:ss Z] ");
        String message = " ******* Proxyma Started" + dateFormatter.format(now) + "*******\n";
        return message;
    }
    // This method is called just after the handler using this formatter is closed

    @Override
    public String getTail(Handler h) {
        Date now = new Date();
        Format dateFormatter = new SimpleDateFormat(" [dd/MMM/yyyy:HH:mm:ss Z] ");
        String message = " ******* Proxyma Shutdown" + dateFormatter.format(now) + "*******\n";
        return message;
    }
}

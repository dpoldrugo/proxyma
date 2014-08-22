package m.c.m.proxyma.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * <p>
 * This custom formatter that "doesn't format" anything.
 * In othed words the messages received from this formatter have to be
 * pre formatted externally.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: OnlyTheMessageFormatter.java 140 2010-06-20 20:30:05Z marcolinuz $
 */
public class OnlyTheMessageFormatter extends Formatter {

    /**
     * Initialize parent class.
     */
    public OnlyTheMessageFormatter () {
        super();
    }

    // This method is called for every log records and puts out only the given message
    @Override
    public String format(LogRecord rec) {
        return rec.getMessage();
    }

    // This method is called just after the handler using this formatter is created
    @Override
    public String getHead(Handler h) {
        return "";
    }
    // This method is called just after the handler using this formatter is closed

    @Override
    public String getTail(Handler h) {
        return "";
    }
}

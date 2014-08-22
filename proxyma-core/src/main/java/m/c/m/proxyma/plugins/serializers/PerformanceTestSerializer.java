package m.c.m.proxyma.plugins.serializers;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import m.c.m.proxyma.ProxymaTags;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.log.ProxymaLoggersUtil;
import m.c.m.proxyma.resource.ProxymaRequest;
import m.c.m.proxyma.resource.ProxymaResource;

/**
 * <p>
 * This plugin extends the Simple Serializer adding to it an ulterior logger.<br/>
 * Its purpose is to write a log file with the time elapsed to serve the
 * requests. It register also if there was a cache-hit.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: PerformanceTestSerializer.java 170 2010-07-02 21:16:10Z marcolinuz $
 */
public class PerformanceTestSerializer extends m.c.m.proxyma.plugins.serializers.SimpleSerializer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public PerformanceTestSerializer (ProxymaContext context) {
        //initialize the base class.
        super(context);

        //Find the specific-plugin section for this plugin xpath to retrive the directives from the configuration file.
        String configXpath = ProxymaTags.AVAILABLE_SERIALIZERS + "[@class='" + this.getClass().getName() + "']";
             
        //Get configuration files direxctives
        String performanceLogDirectory = context.getLogsDirectoryPath();
        int maxLogSize = Integer.parseInt(context.getSingleValueParameter(configXpath+"/@performanceLogMaxLinesPerFile"));
        int logRetention = Integer.parseInt(context.getSingleValueParameter(configXpath+"/@performanceLogRetentionPolicy"));

        //Set up the access-log logger
        performanceLog = Logger.getLogger(ProxymaTags.DEFAULT_LOGGER_PREFIX + "." + context.getName() + ".performances");
        String logFilePath = performanceLogDirectory + "proxyma-" + context.getName() + "-performance.log";
        ProxymaLoggersUtil.initializeCustomLogger(performanceLog, logFilePath, maxLogSize, logRetention);
    }

    /**
     * This method uses its base class to send back to the client the response-data of the resource.<br/>
     * Then It writes the performances log.
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws IOException{
        //send the data to the client using tha base class
        super.process(aResource);

        //Write the performance log
        String logRecord = generatePerformancesLog(aResource);
        performanceLog.finest(logRecord);
    }

    /**
     * Returns the name of the plugin.
     * @return the name of the plugin.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns a short description of what the plugin does..<br/>
     * You can use html tags into it.<br/>
     * The result of this method call can be used by any interface
     * to explain for what is the puropse of the plugin.
     *
     * @return a short description of the plugin
     */
    @Override
    public String getHtmlDescription() {
        return description;
    }

    /**
     * Creates the log line in extended common logging format using the passed
     * resource to obtain the data.
     *
     * @param aResource the resource to inspect for logging data
     * @param statusCode the status code returned by the operation
     * @return the message to write into the access-log
     */
    private String generatePerformancesLog(ProxymaResource aResource) {
        //Create a line in common-log-format into the access.log
        StringBuffer theRecord = new StringBuffer(1024);
        ProxymaRequest request = aResource.getRequest();

        //write the remote ip address
        theRecord.append(request.getRemoteAddress());
        theRecord.append(" - ");

        //write the remote user
        String remoteUser = request.getRemoteUser();
        theRecord.append(remoteUser == null ? "-" : remoteUser);

        //formatting the date
        Date now = new Date();
        theRecord.append(dateFormatter.format(now));

        //get the remote requested resource
        theRecord.append("\"");
        theRecord.append(request.getMethod());
        theRecord.append(" ");
        theRecord.append(request.getRequestURI());
        theRecord.append("\" ");

        //get the return code
        theRecord.append(aResource.getResponse().getResponseData().getStatus());

        //write elapsed time in milliseconds
        theRecord.append(" - ");
        Date before = (Date)aResource.getAttribute("Timestamp");
        if (before != null) {
            long elapsed = now.getTime() - before.getTime();
            theRecord.append(elapsed);
            theRecord.append(" ms.");
        } else {
            theRecord.append(" *** Missing Timestamp Attribute ***");
        }

        //Write if there was a cache hit
        Object cacheHit = aResource.getAttribute(CACHE_HIT_ATTRIBUTE);
        if (cacheHit != null)
            theRecord.append(" CACHE HIT!");
        theRecord.append("\n");
        
        return theRecord.toString();
    }

    /**
     * The logger to write the access log..
     */
    private Logger performanceLog = null;

    /**
     * The date formatter for the common logging format.
     */
    private final Format dateFormatter = new SimpleDateFormat(" [dd/MMM/yyyy:HH:mm:ss Z] ");

    /**
     * The attribute name that will be stored into the resource
     * on every cache-hit.
     */
    private static final String CACHE_HIT_ATTRIBUTE = "Cache-Hit";

    /**
     * The name of this plugin.
     */
    private static final String name = "Performance Test Serializer";
    
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin extends the simple HTTP serializer.<br/>"
            + "Its purpose is to write a log file (named proxyma-${contextName}-performances.log) where "
            + "will be stored the times elapsed to serve the requests.<br/>"
            + "Note: It hilights also if there was a chache-hit.";
}

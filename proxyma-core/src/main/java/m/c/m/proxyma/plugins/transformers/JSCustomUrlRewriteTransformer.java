package m.c.m.proxyma.plugins.transformers;

import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import m.c.m.proxyma.rewrite.URLRewriteEngine;

/**
 * <p>
 * This plugin implements a basic and not complete Javascript Rewriter.<br/>
 * Its scans the html pages and the JS files seraching for javascript links.<br/>
 * If any link is found it will be rewritten it in order to masquerde its real source.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @author Davor Poldrugo (dpoldrugo) [dpoldrugo-at-gmail.com]
 * @version $Id: JSRewriteTransformer.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class JSCustomUrlRewriteTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public JSCustomUrlRewriteTransformer(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
        this.rewriter = new URLRewriteEngine(context);
    }

    /**
     * It scans the HTML page or the JS file contained into the response searching
     * for any URL.<br/>
     * When it finds an URL relative to a configured proxy folders,
     * it uses the UrlRewriterEngine to modify the URL.<br/>
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception {
        ProxymaResponseDataBean originalResponse = aResource.getResponse().getResponseData();
        if (aResource.getDestinationSubPath().contains("ext-grid.js")) {
        	System.out.println("ext-grid.js");
        }
        if (aResource.getDestinationSubPath().contains("jquery")) {
        	log.fine("Skipping " + aResource.getDestinationSubPath());
        	return;
        }
        ProxymaHttpHeader contentType = originalResponse.getHeader(CONTENT_TYPE_HEADER);
        if (contentType == null) {
        	log.fine(String.format("No %s header", CONTENT_TYPE_HEADER));        	
        	return;
        }
        
        // The plugin works only on Javascript documents or fragments
        if (contentType != null && (originalResponse.getData() != null)) {
            if (isProcessableTextResource(aResource)) {
                //Get CSS file content
                log.fine("This is a JS file, searching for JS URLs.");

                String encoding = aResource.getContext().getDefaultEncoding();
                Matcher charsetMatcher = charsetPattern.matcher(contentType.getValue());
                if (charsetMatcher.find())
                    encoding = charsetMatcher.replaceFirst(EMPTY_STRING);

                //Get the original JS Data
                ByteBufferReader reader = ByteBufferFactory.createNewByteBufferReader(originalResponse.getData());
                String content = new String(reader.getWholeBufferAsByteArray(), encoding);

                //Parse and Rewrite JS Data
                String newContent = findAndRewriteJSLinks(content, aResource);

                //Add to the response the rewritten data
                byte[] rewrittenContent = newContent.getBytes(encoding);
                ByteBuffer rewrittenData = ByteBufferFactory.createNewByteBuffer(aResource.getContext());
                rewrittenData.appendBytes(rewrittenContent, rewrittenContent.length);

                //Substitute the page data with the rewritten data
                originalResponse.setData(rewrittenData);
            }
        }
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
     * Guess it the current resource is processable by this plugin
     *
     * @param aResource the current resource
     * @return true if it's a JS file.
     */
    private boolean isProcessableTextResource (ProxymaResource aResource) {
        boolean retValue = false;
        ProxymaHttpHeader contentType = aResource.getResponse().getResponseData().getHeader(CONTENT_TYPE_HEADER);
        Matcher textTypeMatcher = textContentTypePattern.matcher(contentType.getValue());
        Matcher jsTypeMatcher = jsContentTypePattern.matcher(contentType.getValue());

        if (jsTypeMatcher.matches())
            retValue = true;
        else if (textTypeMatcher.matches() && aResource.getRequest().getRequestURI().toLowerCase().endsWith(".js"))
            retValue = true;

        return retValue;
    }

    /**
     * Inspect the passed text searching for JS Links to rewrite and rewrites
     * them using the Url Rewrite Engine.
     *
     * @param content the content of the JS (or a JS fragment)
     * @return the new content with substituted urls.
     */
    private String findAndRewriteJSLinks(String content, ProxymaResource aResource) {
        Matcher linksMatcher  = jsLinksPattern.matcher(content);
        StringBuffer retVal = new StringBuffer(content.length());
        
        //Perform the urls substitution..
        while (linksMatcher.find()) {
            String old = linksMatcher.group(1);
			log.finer("Found URL: " + old);
            String replacedJsUrl = rewriter.masqueradeURL(old, aResource);
            
            String all = linksMatcher.group(0);
            all = all.replace(old, replacedJsUrl);
			linksMatcher.appendReplacement(retVal, all);
        }
        linksMatcher.appendTail(retVal);
        return retVal.toString();
    }

    /**
     * Replaces a JS url with the complete url directive
     * (es: www.a/b/c -> url(/d/b/c) )
     *
     * @param theUrl the url to rewrite.
     * @param aResouce the current resource
     * @return the new url() JS directive.
     */
    private String replaceJSURL (String theUrl, ProxymaResource aResouce) {
        URL proxymaRootURL = aResouce.getProxymaRootURL();
        StringBuffer retValue = new StringBuffer(theUrl.length());
        retValue.append("'");
        retValue.append(proxymaRootURL.getProtocol());
        retValue.append("://");
        retValue.append(proxymaRootURL.getHost());
        if (proxymaRootURL.getPort() > 0)
            retValue.append(":").append(proxymaRootURL.getPort());
        String masqueradeURL = rewriter.masqueradeURL(theUrl, aResouce);
        if (masqueradeURL.toLowerCase().startsWith("http://") || masqueradeURL.toLowerCase().startsWith("https://"))
        	retValue = new StringBuffer("'"+masqueradeURL);
        else {
        	retValue.append(masqueradeURL);
        }
        retValue.append("'");

        log.finer("Rewritten URL to: " + retValue.toString());
        return retValue.toString();
    }

    /**
     * The logger of the context..
     */
    private Logger log = null;
    
    /**
     * The rewriter engine capable to rewrite URLs and Cookies.
     */
    private URLRewriteEngine rewriter = null;

    /**
     * This is the Regular Expressione that does most of the work.
     * It's used to recognize links on CSS and rewrite them with the rewrite engine.
     */
    private static final Pattern jsLinksPattern = Pattern.compile("url\\s*?:.*?(?:\"|')(.*?)(?:\"|')", Pattern.CASE_INSENSITIVE);


    /**
     * One of the values for the content type header that activates this plugin.
     */
    private static final Pattern textContentTypePattern = Pattern.compile("^text/plain.*$", Pattern.CASE_INSENSITIVE);

    /**
     * One of the values for the content type header that activates this plugin.
     */
    private static final Pattern jsContentTypePattern = Pattern.compile("^application/javascript.*$", Pattern.CASE_INSENSITIVE);

    /**
     * Charset match Pattern
     */
    private static final Pattern charsetPattern = Pattern.compile("^.*; *charset *= *", Pattern.CASE_INSENSITIVE);

    /**
     * The content type header
     */
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * The name of this plugin.
     */
    private static final String name = "Custom Javascript URL Rewriter";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is a Custom Basic Javescript Transformer.<br/>"
            + "Its purpose is to scan the html pages and the JavaScript libraries seraching for URLs.<br/>"
            + "If any URL (http...) is found, it will be rewritten it in order to force the client browser "
            + "to use proxyma to retrive it.";

    //INSPECTED ATTRIBUTES
    public final static String SRC = "src";

    //Only an empty string.
    private final static String EMPTY_STRING = "";
}

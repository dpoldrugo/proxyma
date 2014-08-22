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
import org.htmlparser.Attribute;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

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
 * @version $Id: JSRewriteTransformer.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class JSRewriteTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public JSRewriteTransformer(ProxymaContext context) {
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
        ProxymaHttpHeader contentType = originalResponse.getHeader(CONTENT_TYPE_HEADER);
        if (contentType == null) {
        	log.fine(String.format("No %s header", CONTENT_TYPE_HEADER));        	
        	return;
        }
        
        Matcher htmlTypeMatcher = htmlContentTypePattern.matcher(contentType.getValue());

        // The plugin works only on Javascript documents or fragments
        if (contentType != null && (originalResponse.getData() != null)) {
            if (htmlTypeMatcher.matches()) {
                log.fine("This is an Html Page, searching for JS URLs.");
                /**
                 * Inner Class for the html analisys.
                 */
                 final NodeVisitor linkVisitor = new NodeVisitor() {
                    @Override
                    public void visitTag(Tag tag) {
                        String name = tag.getTagName();
                        String tagValue = null;

                        //selects the appropriate action based upon the tag and the attribute types
                        //NOTE: probably this method will be improoved in the future because it doesn't handles
                        //      all the Javascript events. I have also found some problem in the htmlparser
                        //      library with pages that uses lot of javascript.
                        if (SCRIPT.equalsIgnoreCase(name)) {
                            tagValue = tag.getAttribute(SRC);
                            if (tagValue == null) {
                                String Language = ((ScriptTag) tag).getLanguage();
                                if ((Language != null) && (Language.toLowerCase().indexOf(JAVASCRIPT_SEGMENT) >= 0))
                                    ((ScriptTag) tag).setScriptCode(findAndRewriteJSLinks(((ScriptTag) tag).getScriptCode(), currentResource));
                            }
                        } else if (BODY.equalsIgnoreCase(name)) {
                            tagValue = tag.getAttribute(ONLOAD);
                            if (tagValue != null) {
                                tag.removeAttribute(ONLOAD);
                                Attribute attribute = new Attribute();
                                attribute.setName(ONLOAD);
                                attribute.setAssignment("=");
                                attribute.setRawValue("'" + findAndRewriteJSLinks(tagValue, currentResource) + "'");
                                tag.setAttributeEx(attribute);
                            }
                            findAndRewriteJsEvents(tag, currentResource);
                        } else if (A.equalsIgnoreCase(name)     ||
                                   IMG.equalsIgnoreCase(name)   ||
                                   LINK.equalsIgnoreCase(name)  ||
                                   FORM.equalsIgnoreCase(name)  ||
                                   INPUT.equalsIgnoreCase(name) ||
                                   INPUT.equalsIgnoreCase(name) ||
                                   OBJECT.equalsIgnoreCase(name)||
                                   AREA.equalsIgnoreCase(name)  ||
                                   DEL.equalsIgnoreCase(name)   ||
                                   INS.equalsIgnoreCase(name)) {
                            findAndRewriteJsEvents(tag, currentResource);
                        }
                    }
                };


                //Generates a parser for the given page
                String encoding = aResource.getContext().getDefaultEncoding();
                Matcher charsetMatcher = charsetPattern.matcher(contentType.getValue());
                if (charsetMatcher.find())
                    encoding = charsetMatcher.replaceFirst(EMPTY_STRING);

                ByteBufferReader reader = ByteBufferFactory.createNewByteBufferReader(originalResponse.getData());
                String content = new String(reader.getWholeBufferAsByteArray(), encoding);
                Parser parser = new Parser(new Lexer(new Page(content, encoding)));

                //Generate a linkvisitor for the url rewriting
                NodeList myPage = parser.parse(null);
                currentResource = aResource;
                myPage.visitAllNodesWith(linkVisitor);

                //Add to the response the rewritten data
                byte[] rewrittenContent = myPage.toHtml(true).getBytes(encoding);
                ByteBuffer rewrittenData = ByteBufferFactory.createNewByteBuffer(aResource.getContext());
                rewrittenData.appendBytes(rewrittenContent, rewrittenContent.length);

                //Substitute the page data with the rewritten data
                originalResponse.setData(rewrittenData);

            } else if (isProcessableTextResource(aResource)) {
                //Get CSS file content
                log.fine("This is a JS file, searching for JS URLs.");

                String encoding = aResource.getContext().getDefaultEncoding();
                Matcher charsetMatcher = charsetPattern.matcher(contentType.getValue());
                if (charsetMatcher.find())
                    encoding = charsetMatcher.replaceFirst(EMPTY_STRING);

                //Get the original CSS Data
                ByteBufferReader reader = ByteBufferFactory.createNewByteBufferReader(originalResponse.getData());
                String content = new String(reader.getWholeBufferAsByteArray(), encoding);

                //Parse and Rewrite CSS Data
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
     * @return true if it's a CSS file.
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
     * Inspect the passed text searching for cssLinks to rewrite and rewrites
     * them using the Url Rewrite Engine.
     *
     * @param content the content of the CSS (or a css fragment)
     * @return the new content with substituted urls.
     */
    private String findAndRewriteJSLinks(String content, ProxymaResource aResource) {
        Matcher linksMatcher  = jsLinksPattern.matcher(content);
        StringBuffer retVal = new StringBuffer(content.length());
        
        //Perform the urls substitution..
        while (linksMatcher.find()) {
            log.finer("Found URL: " + linksMatcher.group(1));
            linksMatcher.appendReplacement(retVal, replaceJSURL(linksMatcher.group(1), aResource));
        }
        linksMatcher.appendTail(retVal);
        return retVal.toString();
    }

    /**
     * Replaces a cssurl with the complete url directive
     * (es: www.a/b/c -> url(/d/b/c) )
     *
     * @param theUrl the url to rewrite.
     * @param aResouce the current resource
     * @return the new url() css directive.
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
        retValue.append(rewriter.masqueradeURL(theUrl, aResouce));
        retValue.append("'");

        log.finer("Rewritten URL to: " + retValue.toString());
        return retValue.toString();
    }

    /**
     * Search and rewrites urls into common javascript events
     *
     * @param tag the tag to serach for events..
     * @param aResource the current Resource
     */
    private void findAndRewriteJsEvents(Tag tag, ProxymaResource aResource) {
        for (int i = 0; i < EVENTS.length; i++) {
            String tagValue = tag.getAttribute(EVENTS[i]);
            if (tagValue != null) {
                tag.removeAttribute(EVENTS[i]);
                Attribute attribute = new Attribute();
                attribute.setName(EVENTS[i]);
                attribute.setAssignment("=");
                attribute.setRawValue("'" + findAndRewriteJSLinks(tagValue, aResource) + "'");
                tag.setAttributeEx(attribute);
            }
        }
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
     * The only way to share variable between this class and its nested companion
     * is to have private attributes with the wanted values.
     */
    private ProxymaResource currentResource = null;

    /**
     * This is the Regular Expressione that does most of the work.
     * It's used to recognize links on CSS and rewrite them with the rewrite engine.
     */
    private static final Pattern jsLinksPattern = Pattern.compile("(?:\"|\')(http(?:s)?://.*[^\"'])(?:\"|\')", Pattern.CASE_INSENSITIVE);

    /**
     * One of the values for the content type header that activates this plugin.
     */
    private static final Pattern htmlContentTypePattern = Pattern.compile("^text/html.*$", Pattern.CASE_INSENSITIVE);

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
    private static final String name = "Basic Javascript URL Rewriter";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is a Basic Javescript Transformer.<br/>"
            + "Its purpose is to scan the html pages and the JavaScript libraries seraching for URLs.<br/>"
            + "If any URL (http...) is found, it will be rewritten it in order to force the client browser "
            + "to use proxyma to retrive it.";

    //INSPECTED TAG NAMES
    private final static String SCRIPT = "script";
    private final static String BODY = "body";
    private final static String A = "a";
    private final static String IMG = "img";
    private final static String LINK = "link";
    private final static String FORM = "form";
    private final static String INPUT = "input";
    private final static String AREA = "area";
    private final static String INS = "ins";
    private final static String DEL = "del";
    private final static String OBJECT = "del";

    //INSPECTED ATTRIBUTES
    public final static String SRC = "src";
    private final static String ONLOAD = "onLoad";
    private final static String JAVASCRIPT_SEGMENT = "javascript";
    private final static String EVENTS[] = {"onClick", "onRollOver", "onRollOut", "onChange"};

    //Only an empty string.
    private final static String EMPTY_STRING = "";
}

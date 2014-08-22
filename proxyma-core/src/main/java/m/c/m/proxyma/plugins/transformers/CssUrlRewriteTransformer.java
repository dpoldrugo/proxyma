package m.c.m.proxyma.plugins.transformers;

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
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

/**
 * <p>
 * This plugin implements a Style Sheets Rewriter.<br/>
 * Its scans the html pages and the CSS files seraching for style links.<br/>
 * If any link is found it will be rewritten it in order to masquerde its real source.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: CssUrlRewriteTransformer.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class CssUrlRewriteTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public CssUrlRewriteTransformer(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
        this.rewriter = new URLRewriteEngine(context);
    }

    /**
     * It scans the HTML page or the CSS file contained into the response searching for any
     * Stylesheet URL.<br/>
     * When it finds an URL relative to a configured proxy folders,
     * it uses the UrlRewriterEngine to modify the URL.<br/>
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception {
        ProxymaResponseDataBean originalResponse = aResource.getResponse().getResponseData();
        ProxymaHttpHeader contentType = originalResponse.getHeader(CONTENT_TYPE_HEADER);
        Matcher htmlTypeMatcher = htmlContentTypePattern.matcher(contentType.getValue());

        // The plugin works only on Cascading Stylesheet documents or fragments
        if (contentType != null && (originalResponse.getData() != null)) {
            if (htmlTypeMatcher.matches()) {
                log.fine("This is an Html Page, searching for Style URLs.");
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
                        if (STYLE.equalsIgnoreCase(name)) {
                            tagValue = ((StyleTag) tag).getStyleCode();
                            if (tagValue != null) {
                                TextNode data = (TextNode) tag.getFirstChild();
                                data.setText(findAndRewriteStyleLinks(tagValue, currentResource));
                            }
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
                log.fine("This is a CSS file, searching for Style URLs.");

                String encoding = aResource.getContext().getDefaultEncoding();
                Matcher charsetMatcher = charsetPattern.matcher(contentType.getValue());
                if (charsetMatcher.find())
                    encoding = charsetMatcher.replaceFirst(EMPTY_STRING);

                //Get the original CSS Data
                ByteBufferReader reader = ByteBufferFactory.createNewByteBufferReader(originalResponse.getData());
                String content = new String(reader.getWholeBufferAsByteArray(), encoding);

                //Parse and Rewrite CSS Data
                String newContent = findAndRewriteStyleLinks(content, aResource);

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
        Matcher cssTypeMatcher = cssContentTypePattern.matcher(contentType.getValue());

        if (cssTypeMatcher.matches())
            retValue = true;
         else if (textTypeMatcher.matches() && aResource.getRequest().getRequestURI().toLowerCase().endsWith(".css"))
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
    private String findAndRewriteStyleLinks(String content, ProxymaResource aResource) {
        Matcher linksMatcher  = cssLinksPattern.matcher(content);
        StringBuffer retVal = new StringBuffer(content.length());
        
        //Perform the urls substitution..
        while (linksMatcher.find()) {
            log.finer("Found link directive: " + linksMatcher.group(1));
            linksMatcher.appendReplacement(retVal, replaceCssURL(linksMatcher.group(1), aResource));
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
    private String replaceCssURL (String theUrl, ProxymaResource aResouce) {
        StringBuffer retValue = new StringBuffer(128);
        retValue.append("url(").append(rewriter.masqueradeURL(theUrl, aResouce)).append(")");
        log.finer("Rewritten link directive to: " + retValue.toString());
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
     * The only way to share variable between this class and its nested companion
     * is to have private attributes with the wanted values.
     */
    private ProxymaResource currentResource = null;

    /**
     * This is the Regular Expressione that does most of the work.
     * It's used to recognize links on CSS and rewrite them with the rewrite engine.
     */
    private static final Pattern cssLinksPattern = Pattern.compile("(?:url)(?:\\s)?(?:\\()(?:\"|\')?(.*[^\"'])(?:\"|\')?(?:\\))", Pattern.CASE_INSENSITIVE);

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
    private static final Pattern cssContentTypePattern = Pattern.compile("^text/css.*$", Pattern.CASE_INSENSITIVE);

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
    private static final String name = "CSS URL Rewriter";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is a Style Sheet Transformer.<br/>"
            + "Its purpose is to parse html pages and Cascading Style Sheets, seraching for CSS embedded links.<br/>"
            + "If any link is found, it will be rewritten it in order to force the client browser to "
            + "use proxyma to retrive it.";

    //INSPECTED TAG NAMES
    private final static String STYLE = "style";

    //Only an empty string.
    private final static String EMPTY_STRING = "";
}

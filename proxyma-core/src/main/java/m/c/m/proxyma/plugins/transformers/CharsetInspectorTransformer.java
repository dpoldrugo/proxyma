package m.c.m.proxyma.plugins.transformers;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

/**
 * <p>
 * This plugin implements an html Charset inspector.<br/>
 * Its scans the html pages seraching for content-tyle meta tags.<br/>
 * If such tag is found, it will use its value to add a conten-type header
 * to the response.<br/>
 * Note: it only runs if the content-type provided by the remote server does
 * not countain a charset encoding.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: CharsetInspectorTransformer.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class CharsetInspectorTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public CharsetInspectorTransformer(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
    }

    /**
     * If the encoding charset is not passed into the content-type header,
     * It scans the HTML page searching for it.<br/>
     * If it's found, the charset encoding is properly added to the response headers.
     * @param aResource any ProxymaResource
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

        // The plugin works only on Cascading Stylesheet documents or fragments
        if (contentType != null && (originalResponse.getData() != null)) {
            if (htmlTypeMatcher.matches()) {
                log.fine("This is an Html Page, searching for Charset Encoding.");
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
                        if (META.equalsIgnoreCase(name)) {
                            tagValue = tag.getAttribute(HTTP_EQUIV_ATTRIBUTE);
                            if (CONTENT_TYPE_HEADER.equalsIgnoreCase(tagValue)) {
                                Matcher charsetMatcher = charsetPattern.matcher(tag.getAttribute("content"));
                                StringBuffer headerValue = new StringBuffer("text/html; charset=");
                                if (charsetMatcher.find())
                                    headerValue.append(charsetMatcher.replaceFirst(EMPTY_STRING));
                                else
                                    headerValue.append(currentResource.getContext().getDefaultEncoding());
                                log.finer("Setting Content-Type Headert to " + headerValue.toString());
                                currentResource.getResponse().getResponseData().deleteHeader(CONTENT_TYPE_HEADER);
                                currentResource.getResponse().getResponseData().addHeader(CONTENT_TYPE_HEADER, headerValue.toString());
                            }
                        }
                    }
                };


                //Generates a parser to search for the content encoding into the html page
                Matcher charsetMatcher = charsetPattern.matcher(contentType.getValue());
                if (!charsetMatcher.find()) {
                    log.finer("Charset not declared into the HTTP header.. inspect the html code.");
                    ByteBufferReader reader = ByteBufferFactory.createNewByteBufferReader(originalResponse.getData());
                    String content = new String(reader.getWholeBufferAsByteArray(), aResource.getContext().getDefaultEncoding());
                    Parser parser = new Parser(new Lexer(new Page(content, aResource.getContext().getDefaultEncoding())));

                    //Generate a linkvisitor for the url rewriting
                    NodeList myPage = parser.parse(null);
                    currentResource = aResource;
                    myPage.visitAllNodesWith(linkVisitor);
                }
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
     * The logger of the context..
     */
    private Logger log = null;

    /**
     * The only way to share variable between this class and its nested companion
     * is to have private attributes with the wanted values.
     */
    private ProxymaResource currentResource = null;

    /**
     * The value for the content type header that activates this plugin.
     */
    private static final Pattern htmlContentTypePattern = Pattern.compile("^text/html.*$", Pattern.CASE_INSENSITIVE);

    /**
     * Charset match Pattern
     */
    private static final Pattern charsetPattern = Pattern.compile("^.*; *charset *= *", Pattern.CASE_INSENSITIVE);

    /**
     * The content type header
     */
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * The http_equiv html attribute
     */
    private static final String HTTP_EQUIV_ATTRIBUTE = "http-equiv";

    /**
     * The name of this plugin.
     */
    private static final String name = "Charset Inspector Transformer";
    /**
     * A short html description of what it does.
     */
    private static final String description = "" +
              "This plugin is an html inspector.<br/>" +
              "It is very useful if a remote server doesn't provides a proper character encoding " +
              "value into the Content-Type header. In this case, the plugin will scan the html page " +
              "searching for a meta tag that declares it.<br/>" +
              "This plugin can be useful to avoid wrong characters into some html pages.";

    //INSPECTED TAG NAMES
    private final static String META = "meta";

    //Only an empty string.
    private final static String EMPTY_STRING = "";
}

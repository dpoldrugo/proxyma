package m.c.m.proxyma.plugins.transformers;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import m.c.m.proxyma.buffers.ByteBuffer;
import m.c.m.proxyma.buffers.ByteBufferFactory;
import m.c.m.proxyma.buffers.ByteBufferReader;
import m.c.m.proxyma.context.ProxyFolderBean;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.resource.ProxymaHttpHeader;
import m.c.m.proxyma.resource.ProxymaResource;
import m.c.m.proxyma.resource.ProxymaResponseDataBean;
import m.c.m.proxyma.rewrite.URLRewriteEngine;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;

/**
 * <p>
 * This plugin implements an URL Rewriter.<br/>
 * It scans the HTML page contained into the response-data searching for any URL.<br/>
 * When it finds an URL relative to any of the proxy folders configured into the current context,
 * it uses the UrlRewriterEngine to modify the URL.<br/>
 * Its purpose is to make pages and link relative only to proxyma URIs in order to fully
 * masquerde the real source of the resources.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: HtmlUrlRewriteTransformer.java 184 2010-07-20 22:26:21Z marcolinuz $
 */
public class HtmlUrlRewriteTransformer extends m.c.m.proxyma.plugins.transformers.AbstractTransformer {

    /**
     * The default constructor for this class<br/>
     * It prepares the context logger and the logger for the access-log.
     *
     * NOTE: Every plugin must have a constructor that takes a ProxymaContext as parameter.
     */
    public HtmlUrlRewriteTransformer(ProxymaContext context) {
        //initialize the logger
        this.log = context.getLogger();
        this.rewriter = new URLRewriteEngine(context);
    }

    /**
     * It scans the HTML page contained into the response searching for any URL.<br/>
     * When it finds an URL relative to the path of the current configured proxy folders,
     * it uses the UrlRewriterEngine to modify the URL.<br/>
     * @param aResource any ProxymaResource
     */
    @Override
    public void process(ProxymaResource aResource) throws Exception {
        ProxymaResponseDataBean originalResponse = aResource.getResponse().getResponseData();
        ProxymaHttpHeader contentType = originalResponse.getHeader(CONTENT_TYPE_HEADER);
        ProxyFolderBean folder = aResource.getProxyFolder();
        Matcher contentTypeMatcher = processedContentType.matcher(contentType.getValue());

        // The plugin works only on text/html documents
        if ((contentType != null) && (contentTypeMatcher.matches()) && (originalResponse.getData() != null)) {
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
                    if (A.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(HREF);
                        if (tagValue != null) {
                            tag.setAttribute(HREF, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (IMG.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(SRC);
                        if (tagValue != null) {
                            tag.setAttribute(SRC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(ISMAP);
                        if (tagValue != null) {
                            tag.setAttribute(ISMAP, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(USEMAP);
                        if (tagValue != null) {
                            tag.setAttribute(USEMAP, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(LONGDESC);
                        if (tagValue != null) {
                            tag.setAttribute(LONGDESC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (LINK.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(HREF);
                        if (tagValue != null) {
                            tag.setAttribute(HREF, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (FORM.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(ACTION);
                        if (tagValue != null) {
                            tag.setAttribute(ACTION, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (INPUT.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(SRC);
                        if (tagValue != null) {
                            tag.setAttribute(SRC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (TD.equalsIgnoreCase(name)) {
                        //NOTE: This is a NON-Standard attribute for this TAG but MSIE, Netscape and Firefox supports it.
                        //..so I added this statements.
                        tagValue = tag.getAttribute(BACKGROUND);
                        if (tagValue != null) {
                            tag.setAttribute(BACKGROUND, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (TABLE.equalsIgnoreCase(name)) {
                        //NOTE: This is a NON-Standard attribute for this TAG but MSIE, Netscape and Firefox supports it.
                        //..so I added this statements.
                        tagValue = tag.getAttribute(BACKGROUND);
                        if (tagValue != null) {
                            tag.setAttribute(BACKGROUND, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (SCRIPT.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(SRC);
                        if (tagValue != null) {
                            tag.setAttribute(SRC, rewriter.masqueradeURL(tagValue, currentResource));
                        } 
                    } else if (BODY.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(BACKGROUND);
                        if (tagValue != null) {
                            tag.setAttribute(BACKGROUND, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (BASE.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(HREF);
                        if (tagValue != null) {
                            tag.setAttribute(HREF, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (FRAME.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(SRC);
                        if (tagValue != null) {
                            tag.setAttribute(SRC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(LONGDESC);
                        if (tagValue != null) {
                            tag.setAttribute(LONGDESC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (IFRAME.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(SRC);
                        if (tagValue != null) {
                            tag.setAttribute(SRC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(LONGDESC);
                        if (tagValue != null) {
                            tag.setAttribute(LONGDESC, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (APPLET.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(ARCHIVE);
                        if (tagValue != null) {
                            tag.setAttribute(ARCHIVE, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(CODE);
                        if (tagValue != null) {
                            tag.setAttribute(CODE, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(CODEBASE);
                        if (tagValue != null) {
                            tag.setAttribute(CODEBASE, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (OBJECT.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(ARCHIVE);
                        if (tagValue != null) {
                            tag.setAttribute(ARCHIVE, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(CODEBASE);
                        if (tagValue != null) {
                            tag.setAttribute(CODEBASE, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(DATA);
                        if (tagValue != null) {
                            tag.setAttribute(DATA, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                        tagValue = tag.getAttribute(USEMAP);
                        if (tagValue != null) {
                            tag.setAttribute(USEMAP, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (AREA.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(HREF);
                        if (tagValue != null) {
                            tag.setAttribute(HREF, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (DEL.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(CITE);
                        if (tagValue != null) {
                            tag.setAttribute(CITE, rewriter.masqueradeURL(tagValue, currentResource));
                        }
                    } else if (INS.equalsIgnoreCase(name)) {
                        tagValue = tag.getAttribute(CITE);
                        if (tagValue != null) {
                            tag.setAttribute(CITE, rewriter.masqueradeURL(tagValue, currentResource));
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
     * The rewriter engine capable to rewrite URLs and Cookies.
     */
    private URLRewriteEngine rewriter = null;

    /**
     * The only way to share variable between this class and its nested companion
     * is to have private attributes with the wanted values.
     */
    private ProxymaResource currentResource = null;

    /**
     * The value for the content type header that activates this plugin.
     */
    private static final Pattern processedContentType = Pattern.compile("^text/html.*$", Pattern.CASE_INSENSITIVE);

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
    private static final String name = "Html URL Rewriter";
    /**
     * A short html description of what it does.
     */
    private static final String description = ""
            + "This plugin is an HTML Transformer.<br/>"
            + "Its purpose is to parse the html pages searching for http links. Every founded link is modifyed "
            + "in order to force the client browser to use proxyma to retrive it.";

    //INSPECTED TAG NAMES
    private final static String A = "a";
    private final static String APPLET = "applet";
    private final static String AREA = "area";
    private final static String IMG = "img";
    private final static String LINK = "link";
    private final static String FORM = "form";
    private final static String INPUT = "input";
    private final static String SCRIPT = "script";
    private final static String BODY = "body";
    private final static String BASE = "base";
    private final static String FRAME = "frame";
    private final static String IFRAME = "iframe";
    private final static String INS = "ins";
    private final static String DEL = "del";
    private final static String OBJECT = "del";
    private final static String TD = "td";
    private final static String TABLE = "table";

    //INSPECTED TAG ATTRIBUTES
    private final static String HREF = "href";
    private final static String SRC = "src";
    private final static String ACTION = "action";
    private final static String ARCHIVE = "archive";
    private final static String CODE = "code";
    private final static String CODEBASE = "codebase";
    private final static String LONGDESC = "longdesc";
    private final static String ISMAP = "ismap";
    private final static String USEMAP = "usemap";
    private final static String CITE = "cite";
    private final static String DATA = "data";
    private final static String BACKGROUND = "background";

    //Only an empty string.
    private final static String EMPTY_STRING = "";
}

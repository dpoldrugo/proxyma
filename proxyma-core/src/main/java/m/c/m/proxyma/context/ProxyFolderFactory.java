package m.c.m.proxyma.context;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import m.c.m.proxyma.ProxymaTags;
import org.apache.commons.lang.NullArgumentException;

/**
 * <p>
 * This class is a "FactoryMethod" for the ProxyFolderBean Class.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com];
 * @version $Id: ProxyFolderFactory.java 164 2010-06-29 10:59:58Z marcolinuz $
 */
public class ProxyFolderFactory {

    /**
     * Builds a new ProxyFolder to the specified destination setting it up
     * and ready to be attached to a context.
     *
     * @param FolderName the path (and name) of the proxy folder.
     * @param destination the destination URI to masquerade
     * @param context the proxyma context where to take default settings.
     * @throws NullArgumentException if some parameter is null
     * @throws IllegalArgumentException if the folder name or the destination parameter are invalid or malformed
     * @throws UnsupportedEncodingException if the default encoding charset specified on the configuration is not supported.
     */
    public ProxyFolderBean createNewProxyFolder (String FolderName, String destination, ProxymaContext context)
        throws NullArgumentException, IllegalArgumentException, UnsupportedEncodingException {
        ProxyFolderBean theFolder = new ProxyFolderBean(FolderName, destination, context);

        //Set single value parameters
        theFolder.setMaxPostSize(Integer.parseInt(context.getSingleValueParameter(ProxymaTags.FOLDER_MAX_POST_SIZE)));
        theFolder.setCacheProvider(context.getSingleValueParameter(ProxymaTags.FOLDER_CACHEPROVIDER));
        theFolder.setRetriver(context.getSingleValueParameter(ProxymaTags.FOLDER_RETRIVER));
        theFolder.setSerializer(context.getSingleValueParameter(ProxymaTags.FOLDER_SERIALIZER));

        //Set multi value parameters
        Collection<String> preprocessors = context.getMultiValueParameter(ProxymaTags.FOLDER_PREPROCESSORS);
        Iterator<String> iter = preprocessors.iterator();
        while (iter.hasNext())
            theFolder.registerPreprocessor(iter.next());

        Collection<String> transformers = context.getMultiValueParameter(ProxymaTags.FOLDER_TRANSFORMERS);
        iter = transformers.iterator();
        while (iter.hasNext())
            theFolder.registerTransformer(iter.next());

        return theFolder;
    }
}

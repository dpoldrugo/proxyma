package m.c.m.proxyma;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import m.c.m.proxyma.context.ProxymaContext;
import m.c.m.proxyma.core.ProxyEngine;
import m.c.m.proxyma.resource.ProxymaResource;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

/**
 * <p>
 * This is a simple servlet that uses the proxyma-core library to provide
 * a multiple reverse proxy with URL Rewrite capabilities.
 *
 * NOTE: This is only the hook to the reverse-proxy engine. If you are looking
 *       for proxyma intercafe take a look to the proxyma-console webapp.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: ProxymaServlet.java 165 2010-06-30 20:09:51Z marcolinuz $
 */
public class ProxymaServlet extends HttpServlet {

    /**
     * Initialize the servlet and the proxyma environment.
     */
    @Override
    public void init() {
        try {
            //Obtain configuration parameters..
            ServletConfig config = this.getServletConfig();
            String proxymaConfigFile = this.getInitParameter("ProxymaConfigurationFile");
            String proxymaContextName = this.getInitParameter("ProxymaContextName");
            String proxymaLogsDirectory = this.getInitParameter("ProxymaLogsDir");
            
            //if the config file init-parameter is notspecified use the default configuration
            if (proxymaConfigFile == null)
                proxymaConfigFile = config.getServletContext().getRealPath("/WEB-INF/proxyma-config.xml");

            //Hack to get the servlet path reading it directly from the deployment descriptor.
            //Valid until apache will put a getServletMappings() method into the ServletConfig class.
            XMLConfiguration deploymentDescriptor = null;
            try {
                deploymentDescriptor = new XMLConfiguration();
                deploymentDescriptor.setFile(new File(config.getServletContext().getRealPath("/WEB-INF/web.xml")));
                deploymentDescriptor.setValidating(false);
                deploymentDescriptor.load();
            } catch (ConfigurationException ex) {
                Logger.getLogger("").log(Level.SEVERE, "Unable to load web.xml", ex);
            }
            deploymentDescriptor.setExpressionEngine(new XPathExpressionEngine());
            String servletPath = deploymentDescriptor.getString("servlet-mapping[servlet-name='" + config.getServletName() + "']/url-pattern");
            String proxymaServletContext = config.getServletContext().getContextPath() + servletPath.replaceFirst("/\\*$", GlobalConstants.EMPTY_STRING);

            //Check if the logs directory init-parameter ends with "/"
            if (!proxymaLogsDirectory.endsWith("/")) {
                proxymaLogsDirectory = proxymaLogsDirectory + "/";
            }

            //Create a new proxyma facade
            this.proxyma = new ProxymaFacade();

            //Create a new proxyma context
            this.proxymaContext = proxyma.createNewContext(proxymaContextName, proxymaServletContext, proxymaConfigFile, proxymaLogsDirectory);
            
            //Create a reverse proxy engine for this servlet thread
            this.proxymaEngine = proxyma.createNewProxyEngine(proxymaContext);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * This method uses the proxyma-core reverse proxy engine to serve the clients
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            //Obtain a new Proxyma Resource form the sequest and response
            ProxymaResource clientRequest = proxyma.createNewResource(request, response, proxymaContext);
            //process the resource with the proxy engine
            proxymaEngine.doProxy(clientRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex.getMessage());
        }
    } 
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "This is the ProxymaServlet.";
    }// </editor-fold>

    /**
     * The instance of the proxyma-core facade class that will be used to
     * setup the reverse proxy environment
     */
    private ProxymaFacade proxyma = null;

    /**
     * The reverse proxy engine used to handle the client requests
     */
    private ProxyEngine proxymaEngine = null;

    /**
     * The proxyma-context where this webapp will live.
     */
    private ProxymaContext proxymaContext = null;
}

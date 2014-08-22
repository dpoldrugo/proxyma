package m.c.m.proxyma;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is only a servlet simulator to run tests that needs HttpServlet components.
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 * @version $Id: TestServlet.java 138 2010-06-20 13:53:32Z marcolinuz $
 */
public class TestServlet extends HttpServlet {
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            writeSelectMessage( req.getParameter( "color" ), resp.getWriter() );
            setColor( req, req.getParameter( "color" ) );
        }

        void writeSelectMessage( String color, PrintWriter pw ) throws IOException {
            pw.print( "You selected " + color );
            pw.close();
        }

        void setColor( HttpServletRequest req, String color ) throws ServletException {
            req.getSession().setAttribute( "color", color );
        }
}

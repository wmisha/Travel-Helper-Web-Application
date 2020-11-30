import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class IndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // writing to the response
        out.println("<html>");
        out.println("<body>");
        out.println("<h1>Hello, friend! Welcome to our page!</h1>");
        out.println("<p>(<a href=\"/register\">New user? Register here.</a>)</p>");
        out.println("<p>(<a href=\"/login\"> Has an count? Login here.</a>)</p>");
        out.println("<p><a href=\"/login?logout\">(Logout)</a></p>");
        out.println("</body>");
        out.println("</html>");

    }

}

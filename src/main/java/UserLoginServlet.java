import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class UserLoginServlet extends LoginBaseServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();

        String title = "Login";
        String date = getDate();
        String error = request.getParameter("error");
        String errorMessage = "";
        int code = 0;
        if (error != null) {
            try {
                code = Integer.parseInt(error);
            } catch (Exception ex) {
                code = -1;
            }
            errorMessage = getStatusMessage(code);
        }

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/login.html");
        context.put("title",title);
        context.put("errorMessage", errorMessage);
        context.put("date",date);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        if (request.getParameter("newuser") != null) {
            out.println("<p>Registration was successful! Please login in.</p>");
            out.println(writer.toString());
        }

        if (request.getParameter("logout") != null) {
            clearCookies(request, response);
            out.println("<p>Successfully logged out.</p>");
            out.println(writer.toString());
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");

        Status status = dbhandler.authenticateUser(user, pass);

        try {
            if (status == Status.OK) {
                // should eventually change this to something more secure
                response.addCookie(new Cookie("login", "true"));
                response.addCookie(new Cookie("name", user));
                response.sendRedirect(response.encodeRedirectURL("/welcome"));
            }
            else {
                response.addCookie(new Cookie("login", "false"));
                response.addCookie(new Cookie("name", ""));
                response.sendRedirect(response.encodeRedirectURL("/login?error=" + status.ordinal()));
            }
        }
        catch (Exception ex) {
            log.error("Unable to process login form.", ex);
        }
    }

    private void printForm(PrintWriter out) {
        assert out != null;

        out.println("<form action=\"/login\" method=\"post\">");
        out.println("<table border=\"0\">");
        out.println("\t<tr>");
        out.println("\t\t<td>Usename:</td>");
        out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
        out.println("\t</tr>");
        out.println("\t<tr>");
        out.println("\t\t<td>Password:</td>");
        out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<p><input type=\"submit\" value=\"Login\"></p>");
        out.println("</form>");

        out.println("<p>(<a href=\"/register\">new user? register here.</a>)</p>");
    }
}

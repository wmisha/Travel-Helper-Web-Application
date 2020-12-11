package server;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class UserServlet extends BaseServlet {
    /**
     * This method corresponding with the request's Get method.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        String name = getUsername(request);
        // String date = getDate();

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/user.html");

        context.put("name", name);
        //  context.put("date",date);

        context.put("savedHotels", dbhandler.findSavedHotels(getUserId(request)));
        context.put("visitedLinks", dbhandler.findVisitedLinks(getUserId(request)));
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        if (name != null) {
            out.println(writer.toString());
        } else {
            response.sendRedirect("/login");
        }
    }
}

package server;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class UserRegisterServlet extends LoginBaseServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        String title = "Register New User";
        String error = request.getParameter("error");
        String errorMessage = "";
        if(error != null)
             errorMessage= getStatusMessage(error);
        String date = getDate();


        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/register.html");

        context.put("title",title);
        context.put("errorMessage", errorMessage);
        context.put("date",date);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());

        //String content = new String(Files.readAllBytes(Paths.get("templates/register.html")));
        //out.print(content);

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse("Register New User", response);

        String newUser = request.getParameter("user");
        String newPass = request.getParameter("pass");
        Status status = dbhandler.registerUser(newUser, newPass);

        if(status == Status.OK) {
            response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
        }
        else {
            String url = "/register?error=" + status.name();
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }
    }
}

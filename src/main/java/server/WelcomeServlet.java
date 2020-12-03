package server;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handles display of user information.
 * Example of Prof. Engle
 * @see BackEndServer
 */
@SuppressWarnings("serial")
public class WelcomeServlet extends LoginBaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter out = response.getWriter();

		String title = "Welcome";
		String name = getUsername(request);
		String date = getDate();

		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/welcome.html");
		context.put("title",title);
		context.put("name", name);
		context.put("date",date);
		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		if (name != null) {
			out.println(writer.toString());
		}
		else {
			response.sendRedirect("/login");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}
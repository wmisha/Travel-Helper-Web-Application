package server;

import hotelapp.Review;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class SearchReviewServlet extends BaseServlet {


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
        String word = request.getParameter("keyword");
        System.out.println("word: " + word);

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/recommendReviews.html");

        if (word == null) {
            context.put("reviews", new ArrayList());
            return;
        }
        ArrayList<Review> reviews = dbhandler.findReviews(word);
        context.put("reviews", reviews);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {




    }




}

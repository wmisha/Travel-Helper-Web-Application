package server;

import com.google.gson.JsonObject;
import hotelapp.HotelDatabase;
import hotelapp.Review;
import hotelapp.ThreadSafeHotelDatabase;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SearchReviewServlet extends LoginBaseServlet {

    private ThreadSafeHotelDatabase db;

    public SearchReviewServlet(ThreadSafeHotelDatabase db) {
        this.db = db;
    }

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

        ArrayList<Review> reviews = db.getReviewsFromWordMap(word);

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/recommendReviews.html");

        context.put("reviews", reviews);


        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());



//        PrintWriter out = response.getWriter();
//        String word = request.getParameter("word");
//
//        String name = getUsername(request);
//        String date = getDate();
//        ArrayList<Review> reviews = db.getReviewsFromWordMap(word);
//
//        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
//        VelocityContext context = new VelocityContext();
//        Template template = ve.getTemplate("templates/searchReview.html");
//
//        context.put("name", name);
//         context.put("date",date);
//
//        StringWriter writer = new StringWriter();
//        template.merge(context, writer);
//       // out.println(writer.toString());
//
//        if (name != null) {
//            out.println(writer.toString());
//        }
//        else {
//            response.sendRedirect("/login");
//        }


//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        PrintWriter writer = response.getWriter();
//        String word = request.getParameter("word");
//        if (word == null) {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("success", Boolean.FALSE);
//            jsonObject.addProperty("word", "invalid");
//            writer.println(jsonObject.toString());
//            return;
//        }
//        String num = request.getParameter("num");
//        word = StringEscapeUtils.escapeHtml4(word);
//        num = StringEscapeUtils.escapeHtml4(num);
//
//        String words = db.index(word, num);
//        writer.println(words);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {




    }




}

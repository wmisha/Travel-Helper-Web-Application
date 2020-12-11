package server;

import hotelapp.Hotel;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CheckUserReviewsServlet extends BaseServlet {

    public  void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        String name = getUsername(request);
        if (name == null) {
            response.sendRedirect("/login");
            return;
        }

        String date = getDate();
        int userId = dbhandler.findUerIdByUsername(name);
        ArrayList<Review> reviews = dbhandler.findReviewByUserId(userId);


        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/userReviews.html");

//        String hotelId = request.getParameter("hotelId");
//        System.out.println("hotelId: ..........." + hotelId);
//        Hotel hotel = dbhandler.findOneHotelByHotelId(hotelId);
//        ArrayList<Review> reviews = dbhandler.findReviewsByHotelId(hotelId);
//        context.put("hotel", hotel);
//        context.put("hotelId", hotelId);

        context.put("reviews", reviews);
        context.put("date", date);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());


    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        System.out.println(1);

        String reviewId = request.getParameter("reviewId");
        System.out.println("In Edit page: " + reviewId);
        int rating = Integer.parseInt(request.getParameter("rating"));
        String title = request.getParameter("title");
        String text = request.getParameter("text");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        dbhandler.updateReview(reviewId, rating, title, text, date);

        response.sendRedirect("/checkUserReviews");


    }

}

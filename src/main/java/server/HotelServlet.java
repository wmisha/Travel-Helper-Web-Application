package server;

import hotelapp.Hotel;
import hotelapp.Review;
import org.apache.commons.text.StringEscapeUtils;
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
import java.util.List;

public class HotelServlet extends BaseServlet {

    protected static final DatabaseHandler dbHandler = DatabaseHandler.getInstance();


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

        String date = getDate();
        String name = getUsername(request);
        String hotelId = request.getParameter("hotelId");
        System.out.println("hotelId: ..........." + hotelId);
        
        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/hotel.html");

        Hotel hotel = dbHandler.findOneHotelByHotelId(hotelId);
        ArrayList<Review> reviews = dbhandler.findReviewsByHotelId(hotelId);
        context.put("hotel", hotel);
        context.put("reviews", reviews);
        context.put("hotelId", hotelId);
        context.put("date", date);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        if (name != null) {
            out.println(writer.toString());
        } else {
            response.sendRedirect("/login");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        PrintWriter out = response.getWriter();

        String reviewId = dbHandler.getAlphaNumericString(11);
        System.out.println("reviewId: " + reviewId);
        String hotelId = request.getParameter("hotelId");
        System.out.println("hotelId:!!!!!!!!!!! " + hotelId);
        int rating = Integer.parseInt(request.getParameter("rating"));
        String title = request.getParameter("title");
        String text = request.getParameter("text");
        String customer = getUsername(request);
        System.out.println("customer: " + customer);
        String ex = "2016-07-11T19:25:29Z";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("date: " + date);
        int userId = dbHandler.findUerIdByUsername(customer);
        System.out.println("userId: " + userId);

        if (title == null && text == null && customer == null) {
            response.sendRedirect("/hotelInfo");
            return;
        }

        dbHandler.insertValuesToReviews(reviewId, hotelId, rating, title, text, customer, date, userId);

        response.sendRedirect("/hotelInfo?hotelId=" + hotelId);

    }

}

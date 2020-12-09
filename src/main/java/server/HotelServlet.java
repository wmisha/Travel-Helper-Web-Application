package server;

import hotelapp.Review;
import hotelapp.ThreadSafeHotelDatabase;
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
import java.util.List;

public class HotelServlet extends BaseServlet {

    private ThreadSafeHotelDatabase db;

    public HotelServlet(ThreadSafeHotelDatabase db) {
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

        String date = getDate();
        String hotelId = request.getParameter("hotelId");
        System.out.println( "hotelId: ..........." +hotelId);

        String hotelName = db.getSpecificHotelName(hotelId);
        String hotelAddress = db.getSpecificHotelAddress(hotelId);
        List<Review> reviews = db.getReviews(hotelId);
        String name = getUsername(request);


        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/hotel.html");
       context.put("hotelId",hotelId);
       context.put("date",date);
       context.put("hotelName", hotelName);
       context.put("hotelAddress", hotelAddress);
       context.put("reviews", reviews);


        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());

//        if (name != null) {
//            out.println(writer.toString());
//        }
//        else {
//            response.sendRedirect("/login");
//        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        PrintWriter out = response.getWriter();

        String hotelId = request.getParameter("hotelId");
        System.out.println("hotelId:!!!!!!!!!!! " + hotelId);
        String rating = request.getParameter("rating");
        String title = request.getParameter("title");
        String text = request.getParameter("text");
        String customer = getUsername(request);
        System.out.println("customer: " + customer);
        String ex = "2016-07-11T19:25:29Z";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:SS"));
        System.out.println("date: " + date);

        if (rating == null && title == null && text == null && customer==null) {
            response.sendRedirect("/hotelInfo");
            return;
        }
        rating = StringEscapeUtils.escapeHtml4(rating);
        title = StringEscapeUtils.escapeHtml4(title);
        text = StringEscapeUtils.escapeHtml4(text);
        customer = StringEscapeUtils.escapeHtml4(customer);
        db.AddNewReviewToHotelMap(hotelId,Integer.parseInt(rating),title,text,customer,date);
        out.println("<h2>Successfully add a review!</h2>");
        out.println();
        response.sendRedirect("/hotelInfo?hotelId=" + hotelId);

//        String name = getUsername(request);
//        ArrayList<HotelDatabase.HotelMapEntry> hotels;
//
//        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
//        VelocityContext context = new VelocityContext();
//        Template template = ve.getTemplate("templates/recommendHotels.html");


      //  hotels = db.putSuggestionHotelsInJson(city,keyword);
//
//        context.put("name", name);
//        context.put("hotels",hotels);
//
//        StringWriter writer = new StringWriter();
//        template.merge(context, writer);
//        out.println(writer.toString());



    }

}

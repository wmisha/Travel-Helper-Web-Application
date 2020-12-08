package server;

import com.google.gson.JsonObject;
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
import java.util.List;

public class HotelServlet extends HttpServlet {

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
        String hotelId = request.getParameter("hotelId");
        String num = request.getParameter("num");
        String hotelName = db.getSpecificHotelName(hotelId);
        String hotelAddress = db.getSpecificHotelAddress(hotelId);
        List<Review> reviews = db.getReviews(hotelId);
//        String name = getUsername(request);
//        String date = getDate();

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/hotel.html");

       context.put("hotelName", hotelName);
       context.put("hotelAddress", hotelAddress);
       context.put("reviews", reviews);


        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());

















//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_OK);
//
//        PrintWriter writer = response.getWriter();
//        String hotelId = request.getParameter("hotelId");
//        if (hotelId == null) {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("success", Boolean.FALSE);
//            jsonObject.addProperty("hotelId", "invalid");
//            writer.println(jsonObject.toString());
//            return;
//        }
//
//        hotelId = StringEscapeUtils.escapeHtml4(hotelId);
//        String hotelInfo = db.hotelInfo(hotelId);
//        writer.println(hotelInfo);

    }

}

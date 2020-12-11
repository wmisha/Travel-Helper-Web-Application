package server;

import hotelapp.Hotel;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SearchHotelServlet extends BaseServlet {



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
        String name = getUsername(request);

        String city = request.getParameter("city");
        System.out.println("Parameter city:  " + city);
        String keyword = request.getParameter("keyword");
        System.out.println("parameter keyword: " + keyword);

        if (city == null) {
            response.sendRedirect("/searchHotel");
            return;
        }
        if (keyword == null)
            keyword = "";
        ArrayList<Hotel> hotels = dbhandler.findHotels(city, keyword);

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/recommendHotels.html");

        context.put("name", name);
        context.put("hotels", hotels);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());

    }

}

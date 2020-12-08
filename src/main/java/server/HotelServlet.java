package server;

import com.google.gson.JsonObject;
import hotelapp.ThreadSafeHotelDatabase;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter writer = response.getWriter();
        String hotelId = request.getParameter("hotelId");
        if (hotelId == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("hotelId", "invalid");
            writer.println(jsonObject.toString());
            return;
        }

        hotelId = StringEscapeUtils.escapeHtml4(hotelId);
        String hotelInfo = db.hotelInfo(hotelId);
        writer.println(hotelInfo);
    }

}

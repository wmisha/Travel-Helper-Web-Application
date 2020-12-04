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

public class SearchHotelServlet extends HttpServlet {

    private ThreadSafeHotelDatabase db;

    public SearchHotelServlet(ThreadSafeHotelDatabase db) {
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
        String city = request.getParameter("city");
        System.out.println("Parameter city:  " + city);
        String keyword = request.getParameter("keyword");
        System.out.println("parameter keyword: " + keyword);
        if (keyword == null && city == null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("city", "invalid");
            jsonObject.addProperty("keyword", "invalid");
            writer.println(jsonObject.toString());
            return;
        }

        city = StringEscapeUtils.escapeHtml4(city);
        keyword = StringEscapeUtils.escapeHtml4(keyword);
        String hotels = db.putSuggestionHotelsInJson(city,keyword);
        writer.println(hotels);
    }

}

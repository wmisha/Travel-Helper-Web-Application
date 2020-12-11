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

public class UserActions extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String savedHotelId = request.getParameter("saveHotel");
        if (savedHotelId != null) {
            dbhandler.saveHotel(
                    getUserId(request),
                    savedHotelId
            );
        }

        String likedReviewId = request.getParameter("likeReview");
        if (savedHotelId != null) {
            dbhandler.likeReview(
                    getUserId(request),
                    likedReviewId
            );
        }

        String visitedLink = request.getParameter("visitLink");
        if (visitedLink != null) {
            dbhandler.visitLink(
                    getUserId(request),
                    visitedLink
            );
            response.sendRedirect(visitedLink);
        }

        if (request.getParameter("clearSavedHotels") != null) {
            dbhandler.clearSavedHotels(getUserId(request));
            response.sendRedirect("/user");
        }

        if (request.getParameter("clearVisitedLinks") != null) {
            dbhandler.clearVisitedLinks(getUserId(request));
            response.sendRedirect("/user");
        }
    }
}

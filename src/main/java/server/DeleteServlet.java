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

public class DeleteServlet extends BaseServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = getUsername(request);
        if (name == null) {
            response.sendRedirect("/login");
            return;
        }
        String reviewId = request.getParameter("reviewId");
        System.out.println("In delete page: " + reviewId);

        dbhandler.deleteAReviewByReviewId(reviewId);

        response.sendRedirect("/checkUserReviews");
    }
}

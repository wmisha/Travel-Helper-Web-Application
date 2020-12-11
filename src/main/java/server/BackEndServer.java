package server;

import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.nio.file.Path;


public class BackEndServer {
    public static final int PORT = 8090;
    protected static final DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    public void start(){
        Server server = new Server(PORT);
        // Context 1 =
        ServletContextHandler context1 = new ServletContextHandler();
        context1.setContextPath("/");
        context1.addServlet(IndexServlet.class,"/");
        context1.addServlet(UserRegisterServlet.class, "/register");
        context1.addServlet(UserLoginServlet.class, "/login");
        context1.addServlet(WelcomeServlet.class, "/welcome");
        context1.addServlet(new ServletHolder(new SearchHotelServlet()), "/searchHotel");
        context1.addServlet(new ServletHolder(new HotelServlet()), "/hotelInfo");
        context1.addServlet(new ServletHolder(new SearchReviewServlet()), "/searchReview");
        context1.addServlet(new ServletHolder(new CheckUserReviewsServlet()), "/checkUserReviews");
        context1.addServlet(new ServletHolder(new DeleteServlet()), "/deleteAReview");


        // initialize Velocity
        VelocityEngine velocity = new VelocityEngine();
        velocity.init();

        // set velocity as an attribute of the context so that we can access it
        // from servlets
        context1.setContextPath("/");
        context1.setAttribute("templateEngine", velocity);

        ServletContextHandler context2 = new ServletContextHandler();
        context2.setContextPath("/hotel");
      //  context2.addServlet(HotelInfoServlet.class, "/");
        context2.addServlet(SearchReviewServlet.class, "/review");


        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context1, context2});

        server.setHandler(handlers);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){


        // 1. ThreadSafeParseFiles passFiles = new ThreadSafeParseFiles(3);
        // 2. ThreadSafeHotelDatabase db = new ThreadSafeHotelDatabase(passFiles);
        //            |
        //             \
        //               --->  passFiles.parseData();
        //                     db will get an instance of the singleton DatabaseHandler
        //                     MySQL "INSERT INTO" statements for Hotels and Reviews.

       BackEndServer backEndServer = new BackEndServer();
     //  dbHandler.populateTables("input/hotels/hotels.json","input/reviews");

        try {
            backEndServer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

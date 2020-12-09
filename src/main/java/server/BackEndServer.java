package server;

import hotelapp.HotelSearch;
import hotelapp.ThreadSafeHotelDatabase;
import hotelapp.ThreadSafeParseFiles;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.HashMap;
import java.util.Map;


public class BackEndServer {
    public static final int PORT = 8090;
    private ThreadSafeHotelDatabase resources;

    public BackEndServer(ThreadSafeHotelDatabase resources) {
        this.resources = resources;
    }

    public void start(){
        Server server = new Server(PORT);
        // Context 1 =
        ServletContextHandler context1 = new ServletContextHandler();
        context1.setContextPath("/");
        context1.addServlet(IndexServlet.class,"/");
        context1.addServlet(UserRegisterServlet.class, "/register");
        context1.addServlet(UserLoginServlet.class, "/login");
        context1.addServlet(WelcomeServlet.class, "/welcome");
        context1.addServlet(new ServletHolder(new SearchHotelServlet(resources)), "/searchHotel");
        context1.addServlet(new ServletHolder(new HotelServlet(resources)), "/hotelInfo");
        context1.addServlet(new ServletHolder(new SearchReviewServlet(resources)), "/searchReview");


        // initialize Velocity
        VelocityEngine velocity = new VelocityEngine();
        velocity.init();

        // set velocity as an attribute of the context so that we can access it
        // from servlets
        context1.setContextPath("/");
        context1.setAttribute("templateEngine", velocity);

        ServletContextHandler context2 = new ServletContextHandler();
        context2.setContextPath("/hotel");
        context2.addServlet(HotelInfoServlet.class, "/");
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
        if (args.length < 2) {
            System.out.println("provide -hotels or -reviews");
            System.exit(-1);
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i = i + 2) {
            map.putIfAbsent(args[i], args[i + 1]);
        }
        String hotelsFile = map.get("-hotels");
        String reviewsDir = map.get("-reviews");

        ThreadSafeHotelDatabase db = new ThreadSafeHotelDatabase();
        ThreadSafeParseFiles passFiles = new ThreadSafeParseFiles(Integer.parseInt("3"));
        HotelSearch hotelSearch = new HotelSearch(db, passFiles);
        hotelSearch.getDataReady(hotelsFile, reviewsDir);

        BackEndServer backEndServer = new BackEndServer(db);
        try {
            backEndServer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
